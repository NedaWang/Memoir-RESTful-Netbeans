/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package memoir.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
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
import memoir.Person;

/**
 *
 * @author neda
 */
@Stateless
@Path("memoir.person")
public class PersonFacadeREST extends AbstractFacade<Person> {

    @PersistenceContext(unitName = "MemoirDBPU")
    private EntityManager em;

    public PersonFacadeREST() {
        super(Person.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(Person entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") Integer id, Person entity) {
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
    public Person find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Person> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Person> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
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
    
    
    //firstname
    @GET
    @Path("findByFirstname/{firstname}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Person> findByFirstname(@PathParam("firstname") String firstname) {
        Query query = em.createNamedQuery("Person.findByFirstname");
        query.setParameter("firstname",firstname);
        return query.getResultList();
    }
    
    //surname
    @GET
    @Path("findBySurname/{surname}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Person> findByLastname(@PathParam("surname") String surname){
        Query query = em.createNamedQuery("Person.findBySurname");
        query.setParameter("surname",surname);
        return query.getResultList();
    }
    
    //dob
    //define a formate
    @GET
    @Path("findByDob/{dob}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Person> findByDob(@PathParam("dob") String dob) throws ParseException{
        Query query = em.createNamedQuery("Person.findByDob");
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dob);
        query.setParameter("dob",date);
        return query.getResultList();
    }
    
    //combine address, postcode, state
    @GET
    @Path("findByAddressStatePostcode/{address}/{state}/{postcode}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Person> findByAddressStatePostcode(@PathParam("address") String address, 
            @PathParam("state") String state, @PathParam("postcode") int postcode){
        TypedQuery query = em.createQuery("SELECT p FROM Person p WHERE p.address = :address "
                + "AND p.postcode = :postcode AND p.state = :state", Person.class);
        query.setParameter("address",address);
        query.setParameter("state",state);
        query.setParameter("postcode",postcode);
        return query.getResultList();
    }
    
    
}
