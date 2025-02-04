package com.portfolio.security;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.portfolio.data.utils.ConfigUtils;


public class ConnexionLdap {
	
	private Hashtable<String, String> env;
	private ArrayList<Attribute> attr;
	private String site;

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public Hashtable<String, String> getEnv() {
		return env;
	}

	public void setEnv(Hashtable<String, String> env) {
		this.env = env;
	}

	public ArrayList<Attribute> getAttr() {
		return attr;
	}

	public void setAttr(ArrayList<Attribute> attr) {
		this.attr = attr;
	}


	public String[] getLdapValue( String usern ) throws NamingException, IOException {

		Attributes matchAttrs = new BasicAttributes(true);

		// recuperation des propriétés
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ConfigUtils.get("ldap.provider.url"));
		
		String username = ConfigUtils.get("ldap.context.name");
		String password = ConfigUtils.get("ldap.context.credential");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, username);
		env.put(Context.SECURITY_CREDENTIALS, password);
		
		String checkSSL = ConfigUtils.get("ldap.provider.useSSL");
		if( checkSSL != null && "Y".equals(checkSSL.toUpperCase()) )
			env.put(Context.SECURITY_PROTOCOL,"ssl");
//		env.put("java.naming.ldap.factory.socket", "javax.net.ssl.SSLSocketFactory");

		/// Limit return values
		String attribFirstN = ConfigUtils.get("ldap.user.firstname");
		String attribLastN = ConfigUtils.get("ldap.user.lastname");
		String attribMail = ConfigUtils.get("ldap.user.mail");
		String returnAttrib[] = {attribFirstN, attribLastN, attribMail};
		
		SearchControls controle = new SearchControls();
		controle.setReturningAttributes(returnAttrib);
		controle.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		String checkParam = ConfigUtils.get("ldap.parameter");
		String critere  = checkParam.replace("%u", usern); //filtre LDAP avec %u = userid (cas)
		//String critere = String.format("(%s=%s)", checkParam, usern);
		
		DirContext ictx = new InitialDirContext(env);
		String contextName = ConfigUtils.get("ldap.baseDn");
		NamingEnumeration<SearchResult> e = ictx.search(contextName, critere, controle);
		String retval = null;
		String fname = null;
		String lname = null;
		String mail = null;
		if (e.hasMore()) {
			SearchResult r = e.next();

			Attributes attribs = r.getAttributes();
			Attribute fobj = attribs.get(attribFirstN);
			if( fobj != null ) fname = fobj.get().toString();
			else fname = "";
			
			Attribute lobj = attribs.get(attribLastN);
			if( lobj != null ) lname = lobj.get().toString();
			else lname = "";
			
			Attribute mobj = attribs.get(attribMail);
			if( mobj != null ) mail = mobj.get().toString();
			else mail = "";
		}
		ictx.close();// fermeture de la connexion au ldap
		
		return new String[]{retval, fname, lname, mail};
	}

	public void listerAttributs(Attributes atts)
			throws javax.naming.NamingException {
		
		String displayName;
		String givenName;
		String sn;
		String mail;
		//String affiliation;
		
		displayName = (String) atts.get("displayName").get();
		givenName = (String) atts.get("givenName").get();
		sn = (String) atts.get("sn").get();
		mail = (String) atts.get("mail").get();

		 System.out.println(displayName);
		 System.out.println(mail);
		 

	}
}
