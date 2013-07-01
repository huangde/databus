package controllers.gui;

import gov.nrel.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Entity;
import models.EntityGroup;
import models.EntityGroupXref;
import models.EntityUser;
import models.SecureResourceGroupXref;
import models.SecureSchema;
import models.comparitors.EntityGroupComparitor;
import models.comparitors.SecureSchemaComparitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import play.mvc.With;

import com.alvazan.play.NoSql;

import controllers.gui.auth.GuiSecure;

@With(GuiSecure.class)
public class MyDatabus extends Controller {
	static final Logger log = LoggerFactory.getLogger(MyDatabus.class);
	
	/**
	 * Static variables so we dont constantly have to do logic in every
	 * method
	 */
	private static List<EntityGroup> allGroups;
	private static List<SecureSchema> allDatabases;
	
	private static List<EntityGroup> systemGroups = new ArrayList<EntityGroup>();
	private static List<EntityGroup> adminGroups = new ArrayList<EntityGroup>();
	private static List<EntityGroup> memberGroups = new ArrayList<EntityGroup>();
	
	
	private static List<SecureSchema> systemDatabases = new ArrayList<SecureSchema>();
	private static Map<SecureSchema, String> readableDatabases = new HashMap<SecureSchema, String>();
	private static Map<SecureSchema, String> readwriteDatabases = new HashMap<SecureSchema, String>();
	private static Map<SecureSchema, String> myDatabases = new HashMap<SecureSchema, String>();
	
	public static void myDatabus() {
		EntityUser user = Utility.getCurrentUser(session);
		
		MyDatabus.refreshSystemInformation();
		
		List<EntityGroup> systemGroups =MyDatabus.systemGroups;
		List<EntityGroup> adminGroups = MyDatabus.adminGroups;
		List<EntityGroup> memberGroups = MyDatabus.memberGroups;		
		
		Collections.sort(systemGroups, new EntityGroupComparitor());
		Collections.sort(adminGroups, new EntityGroupComparitor());
		Collections.sort(memberGroups, new EntityGroupComparitor());
			
		List<SecureSchema> systemDatabases = MyDatabus.systemDatabases;
		Map<SecureSchema, String> readableDatabases = MyDatabus.readableDatabases;
		Map<SecureSchema, String> readwriteDatabases = MyDatabus.readwriteDatabases;
		Map<SecureSchema, String> myDatabases = MyDatabus.myDatabases;
		
		Collections.sort(systemDatabases, new SecureSchemaComparitor());
		
		render(user, adminGroups, memberGroups, systemGroups, systemDatabases, myDatabases, readableDatabases, readwriteDatabases);
	}
	
	public static void databusSummary() {
		EntityUser user = Utility.getCurrentUser(session);
		
		MyDatabus.refreshSystemInformation();
		
		int systemGroupCount =MyDatabus.systemGroups.size();
		int  adminGroupCount = MyDatabus.adminGroups.size();
		int  memberGroupCount = MyDatabus.memberGroups.size();			
					
		int group_count = systemGroupCount + adminGroupCount + memberGroupCount;
		int database_count = MyDatabus.allDatabases.size();
		
		List<EntityGroup> systemGroups =MyDatabus.systemGroups;
		List<EntityGroup> adminGroups = MyDatabus.adminGroups;
		List<EntityGroup> memberGroups = MyDatabus.memberGroups;	
		
		Collections.sort(systemGroups, new EntityGroupComparitor());
		Collections.sort(adminGroups, new EntityGroupComparitor());
		Collections.sort(memberGroups, new EntityGroupComparitor());
		
		//systemGroups.get(0).getChildren().size()
		//systemGroups.get(0).getResources().size()
			
		List<SecureSchema> systemDatabases = MyDatabus.systemDatabases;
		List<SecureSchema> readableDatabases = new ArrayList<SecureSchema>(MyDatabus.readableDatabases.keySet());
		List<SecureSchema> readwriteDatabases =  new ArrayList<SecureSchema>(MyDatabus.readwriteDatabases.keySet());
		List<SecureSchema> myDatabases =  new ArrayList<SecureSchema>(MyDatabus.myDatabases.keySet());
		
		Collections.sort(systemDatabases, new SecureSchemaComparitor());
		Collections.sort(readableDatabases, new SecureSchemaComparitor());
		Collections.sort(readwriteDatabases, new SecureSchemaComparitor());
		Collections.sort(myDatabases, new SecureSchemaComparitor());
		
		render(user, group_count, database_count, myDatabases, systemDatabases,
				   readableDatabases, readwriteDatabases, adminGroups, systemGroups, memberGroups);
	}
	
	public static void databusDatabases() {
		EntityUser user = Utility.getCurrentUser(session);
		
		List<SecureSchema> databases = SecureSchema.findAll(NoSql.em());
		
		render(databases);
	}
	
	public static void databusGroups() {
		EntityUser user = Utility.getCurrentUser(session);
		
		List<EntityGroup> groups = EntityGroup.findAll(NoSql.em());
		
		render(groups);
	}
	
	private static void refreshSystemInformation() {
		EntityUser user = Utility.getCurrentUser(session);
		
		MyDatabus.allGroups = EntityGroup.findAll(NoSql.em());
		MyDatabus.allDatabases = SecureSchema.findAll(NoSql.em());
		
		MyDatabus.systemGroups = new ArrayList<EntityGroup>();
		MyDatabus.adminGroups = new ArrayList<EntityGroup>();
		MyDatabus.memberGroups = new ArrayList<EntityGroup>();
		
		
		MyDatabus.systemDatabases = new ArrayList<SecureSchema>();
		MyDatabus.readableDatabases = new HashMap<SecureSchema, String>();
		MyDatabus.readwriteDatabases = new HashMap<SecureSchema, String>();
		MyDatabus.myDatabases = new HashMap<SecureSchema, String>();

		for (SecureSchema db : allDatabases) {
			db.populateTableCount(NoSql.em());
			
			List<SecureResourceGroupXref> refs = db.getEntitiesWithAccess();
			
			boolean foundUser = false;
			
			for (SecureResourceGroupXref ref : refs) {
				Entity entity = ref.getUserOrGroup();
								
				if(entity.getName() == null) {
					continue;
				}
				
				if(entity.getClassType().toLowerCase().equals("userimpl")) {
					if(entity.getName().equals(user.getName())) {
						foundUser = true;
						/**
						 * Ok we found the current user in the list here... lets remember their permission
						 */
						switch(ref.getPermission()) {
							case ADMIN: {
								myDatabases.put(db, user.getName());
								break;
							}
							case READ: {
								readableDatabases.put(db, user.getName());
								break;
							}
							case READ_WRITE: {
								readwriteDatabases.put(db, user.getName());
								break;
							}
							default: {
								systemDatabases.add(db);
								break;
							}
						}
						
						/**
						 * We'll continue because its possible that the user might also be in a group
						 * associated w/ this database
						 */
					}
				} else if(entity.getClassType().toLowerCase().equals("group")) {
					EntityGroup group = (EntityGroup)entity;
					List<EntityGroupXref> users = group.getChildren();
					
					for(EntityGroupXref member : users) {
						if(member.getEntity() == null) {
							continue;
						}
						
						if(member.getEntity().getName() == null) {
							continue;
						}
						
						if(member.getEntity().getName().equals(user.getName())) {
							foundUser = true;
							
							switch(ref.getPermission()) {
								case ADMIN: {
									myDatabases.put(db, group.getName() + " (Group)");
									break;
								}
								case READ: {
									readableDatabases.put(db, group.getName() + " (Group)");
									break;
								}
								case READ_WRITE: {
									readwriteDatabases.put(db, group.getName() + " (Group)");
									break;
								}
								default: {
									systemDatabases.add(db);
									break;
								}
							}
						}						
					}
				}
			}
			
			if(!foundUser) {
				systemDatabases.add(db);
			}
		}
		
		for(EntityGroup group : allGroups) {
			List<EntityGroupXref> childGroups = group.getChildren();
			
			String groupName = group.getName();
			if((groupName == null) || (groupName.equals(""))) {
				continue;
			}
			
			boolean foundUser = false;
			for(EntityGroupXref childGroup : childGroups) {
				
				if(childGroup.getEntity().getName().equals(user.getName())) {
					foundUser = true;
					
					if(childGroup.isGroupAdmin()) {
						adminGroups.add(group);
					} else {
						memberGroups.add(group);
					}
					break;
				}
			}
			
			if(!foundUser) {
				systemGroups.add(group);
			}
		}
	}

}