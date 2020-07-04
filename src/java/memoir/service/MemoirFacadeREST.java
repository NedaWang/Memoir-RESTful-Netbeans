/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memoir.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import memoir.Memoir;
import java.text.DateFormatSymbols;

/**
 *
 * @author neda
 */
@Stateless
@Path("memoir.memoir")
public class MemoirFacadeREST extends AbstractFacade<Memoir> {

    @PersistenceContext(unitName = "MemoirDBPU")
    private EntityManager em;

    public MemoirFacadeREST() {
        super(Memoir.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(Memoir entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Integer id, Memoir entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Memoir find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Memoir> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Memoir> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    //3c
    @GET
    @Path("findByPostcodeAndMovieName/{postcode}/{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Memoir> findByPostcodeAndMovieName(@PathParam("postcode") int postcode, @PathParam("name") String name) {
        TypedQuery query = em.createQuery("SELECT m FROM Memoir m WHERE m.cinema.postcode = :postcode AND m.name = :name", Memoir.class);
        query.setParameter("postcode",postcode);
        query.setParameter("name",name);
        return query.getResultList();
    }
    
    //3d
    @GET
    @Path("findByWatchDateAndCinemaName/{watchdate}/{name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Memoir> findByWatchdateAndCinemaName(@PathParam("watchdate") Date watchdate, @PathParam("name") String name) throws ParseException{
        Query query = em.createNamedQuery("Memoir.findByWatchdateAndCinemaName");
        //Date date = new SimpleDateFormat("yyyy-MM-dd").parse(watchdate);
        query.setParameter("watchdate",watchdate);
        query.setParameter("name",name);
        return query.getResultList();
    }
    
    
    //4a
    @GET
    @Path("findByDates/{startdate}/{enddate}/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Object findByDates(@PathParam("id") int id, @PathParam("startdate") Date startdate, @PathParam("enddate") Date enddate) throws ParseException {
     
        TypedQuery query = em.createQuery("SELECT COUNT(m.cinema.postcode),m.cinema.postcode "
                + "FROM Memoir AS m WHERE m.watchdate <= :enddate AND m.watchdate >= :startdate "
                + "AND m.personid.id = :id GROUP BY m.cinema.postcode", Object[].class);
  
        query.setParameter("enddate",enddate);
        query.setParameter("startdate",startdate);
        query.setParameter("id",id);
        List<Object[]> movies =  query.getResultList();
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] movie : movies){
            JsonObject personObject = Json.createObjectBuilder()
                  .add("number", (String)movie[0].toString())
                  .add("postcode",(String)movie[1].toString())
                  .build();
            arrayBuilder.add(personObject);
        }
        JsonArray jArray = arrayBuilder.build();
        return jArray;
    } 
   
    
    //4b
    /*
    SELECT month(m.watchdate),count(*)
    from memoir as m 
    where year(m.RELEASEDATE)=2020 and m.PERSONID = 1
    group by month(m.releasedate);

    */
    @GET
    @Path("findByYear/{year}/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Object findByYear(@PathParam("year") String year, @PathParam("id") int id){

        Query query = em.createNativeQuery("SELECT month(watchdate),count(*) FROM Memoir AS m "
                + "WHERE YEAR(watchdate) = ? and personid = ? group by month(watchdate)");
        query.setParameter(1,year);
        query.setParameter(2,id);
        List<Object[]> movies =  query.getResultList();
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] movie : movies){
            JsonObject personObject = Json.createObjectBuilder()
                  .add("month",new DateFormatSymbols().getMonths()[(Integer)movie[0]-1])
                  .add("number",(Integer)movie[1])
                  //.add(new DateFormatSymbols().getMonths()[(Integer)movie[0]-1],(Integer)movie[1])
                  //.add("postcode",(String)movie[1].toString())
                  .build();
            arrayBuilder.add(personObject);
        }
        JsonArray jArray = arrayBuilder.build();
        return jArray;
    }



    //4c
    @GET
    @Path("findByHighestScore/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object findByHighestScore(@PathParam("id") int id){
        Query query = em.createQuery("SELECT m.name, m.score, m.releasedate FROM Memoir AS m WHERE m.score = ("
                + "SELECT MAX(m.score) FROM Memoir AS m WHERE m.personid.id = :id)", Object[].class);
        query.setParameter("id",id);
        List<Object[]> movies =  query.getResultList();
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] movie : movies){
            JsonObject personObject = Json.createObjectBuilder()
                  .add("name", (String)movie[0])
                  .add("score", (Integer)movie[1])
                  .add("releasedate",(String)movie[2].toString())
                  .build();
            arrayBuilder.add(personObject);
        }
        JsonArray jArray = arrayBuilder.build(); 
        return jArray;        
    }
    
    //4d
    @GET
    @Path("findByWatchyearAndPersonid/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object findByCurrentYearAndPersonid(@PathParam("id") int id){
        //select m.name,year(m.releasedate) from Memoir as m where m.ID in 
        //(select m.ID from memoir m where m.PERSONID = 1) and year(m.releasedate)=year(current_timestamp);
        Query query = em.createNativeQuery("SELECT name,year(releasedate) FROM MEMOIR where year(RELEASEDATE) = year(watchdate) and personid =?");
        query.setParameter(1,id);
        List<Object[]> movies =  query.getResultList();
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] movie : movies){
            JsonObject personObject = Json.createObjectBuilder()
                  .add("name", (String)movie[0])
                  .add("year", (Integer)movie[1])
                  //.add("releasedate",(String)movie[2].toString())
                  .build();
            arrayBuilder.add(personObject);
        }
        JsonArray jArray = arrayBuilder.build(); 
        return jArray;        
    }
    
    
    //4eeeeeee
    /*
    select m.name,m.releasedate from memoir as m where m.name in
        (select m.name from memoir as m 
            where m.PERSONID = 1 
            group by m.name
            having count(m.RELEASEDATE) > 1);
    */
    @GET
    @Path("findByRemake/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Object findByRemake(@PathParam("id") int id){
        Query query = em.createNativeQuery("select name,releasedate from memoir where personid = ? "
                + "AND name in (select name from memoir " +
                "where PERSONID = ? group by name having count(RELEASEDATE) > 1)");
        query.setParameter(1,id);
        query.setParameter(2,id);
        List<Object[]> movies =  query.getResultList();
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] movie : movies){
            JsonObject personObject = Json.createObjectBuilder()
                  .add("name", (String)movie[0])
                  .add("releasedate",(String)movie[1].toString())
                  .build();
            arrayBuilder.add(personObject);
        }
        JsonArray jArray = arrayBuilder.build(); 
        return jArray;        
    }
    
    //4fffffff
    /*
    select m.name,m.RELEASEDATE,m.SCORE from memoir as m 
        where year(m.RELEASEDATE) = year(current_timestamp) 
        and m.PERSONID =1 order by m.score desc fetch first 5 rows only;
    */
    @GET
    @Path("findTopFiveByIdInCurrentYear/{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public Object findTopFiveByIdInCurrentYear(@PathParam("id") int id){
        Query query = em.createNativeQuery("select name,RELEASEDATE,SCORE from memoir " +
    "where year(RELEASEDATE) = year(current_timestamp) and PERSONID =? order by score desc fetch first 5 rows only");
        query.setParameter(1,id);
        List<Object[]> movies =  query.getResultList();
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Object[] movie : movies){
            JsonObject personObject = Json.createObjectBuilder()
                  .add("name", (String)movie[0])
                  .add("releasedate",(String)movie[1].toString())
                  .add("score", (Integer)movie[2])
                  .build();
            arrayBuilder.add(personObject);
        }
        JsonArray jArray = arrayBuilder.build(); 
        return jArray;        
    }
    
    @GET
    @Path("findByPersonID/{personid}")
    @Produces({ MediaType.APPLICATION_JSON})
    public List<Memoir> findByUserID(@PathParam("personid") int personid) {
        TypedQuery query = em.createQuery("SELECT m FROM Memoir m WHERE m.personid.id = :personid", Memoir.class);
        query.setParameter("personid",personid);
        return query.getResultList();
    }
  
}
