package com.jlava.dao;

//import com.jlava.persistence.HibernateUtil;
import com.jlava.model.Person;
import com.jlava.model.Role;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.stat.Statistics;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;

//@Transactional
public class RoleDao {
	SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Long addRole(Role role) {
		role.setDeleted(false);
		Session session = sessionFactory.getCurrentSession();

		return (Long)session.save(role);
	}

	public int updateRole(Role role) {
		Session session = sessionFactory.getCurrentSession();
		session.update(role);
		return 1;
	}

	public int deleteRole(Role role) {
		role.setDeleted(true);
		int deleted = updateRole(role);

		return deleted;
	}

	public List<Role> getRoles() {
		Statistics stats = sessionFactory.getStatistics();
		stats.setStatisticsEnabled(true);

		printLog("\nPRIOR");
		printStats(stats);

		Session session = sessionFactory.getCurrentSession();
		List<Role> roles = null;
		roles = session.createQuery("FROM Role WHERE deleted=false ORDER BY id ASC").setCacheable(true).list();
		
		printLog("\nEND");
		printStats(stats);

		return roles;
	}

	public Role getRole(Long id) {
		Session session = sessionFactory.getCurrentSession();
		Role role = (Role)session.createQuery("FROM Role WHERE deleted=false AND id = :id")
						.setParameter("id", id).uniqueResult();
		return role;
	}

	public List<Person> getPersonsWithRole(Long id) {
		return getRole(id).getPersons();
	}

	private void printStats(Statistics stats) {
		System.out.println("Fetch Count=" + stats.getEntityFetchCount());
		System.out.println("Query Hit = " + stats.getQueryCacheHitCount());
		System.out.println("Second Level Hit Count=" + stats.getSecondLevelCacheHitCount());
		System.out.println("Second Level Miss Count=" + stats.getSecondLevelCacheMissCount());
		System.out.println("Second Level Put Count=" + stats.getSecondLevelCachePutCount());
	}
	
	private void printLog(String msg) {
		System.out.println(msg);
	}	
}