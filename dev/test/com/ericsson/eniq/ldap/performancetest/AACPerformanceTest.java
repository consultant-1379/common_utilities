package com.ericsson.eniq.ldap.performancetest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.ericsson.eniq.ldap.handler.IHandler;
import com.ericsson.eniq.ldap.handler.PermissionGroupHandler;
import com.ericsson.eniq.ldap.handler.RoleHandler;
import com.ericsson.eniq.ldap.handler.UserHandler;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;
import com.ericsson.eniq.ldap.vo.RoleVO;
import com.ericsson.eniq.ldap.vo.UserVO;

/**
 * Performance test cases for LDAP management classes.
 *
 * These test cases requires an LDAP server having ENIQ Events LDAP schema.
 *
 * Before running test cases, add the following entry to your hosts file: <IP Address of LDAP Server> ldapserver
 *
 * To run the test cases, uncomment junit annotations in test methods.
 *
 * Do not checkin with test enabled, as it will fail in CI build.
 *
 * initializeLDAP() is setup method and complete() is tearDown method.
 *
 * @author eramano
 *
 */
public class AACPerformanceTest {

    static long startTime = 0;

    static long endTime = 0;

    final static List<String> predefinedPerms = new ArrayList<String>();
    static {
        predefinedPerms.add("eventsui.network.view");
        predefinedPerms.add("eventsui.ranking.view");
        predefinedPerms.add("eventsui.subscriber.view");
        predefinedPerms.add("eventsui.terminal.view");
    }

    final static List<String> predefinedPermGroups = new ArrayList<String>();
    static {
        predefinedPermGroups.add("allpermissions");
        predefinedPermGroups.add("networkgroup");
        predefinedPermGroups.add("subscribergroup");
        predefinedPermGroups.add("terminalgroup");
        predefinedPermGroups.add("rankinggroup");
    }

    IHandler userHandler;

    IHandler roleHandler;

    IHandler permGroupHandler;

    @Test
    public void testNothing() {
        Assert.assertTrue("Placeholder testcase for CI", true);
    }

    //@Before
    public void initializeLDAP() {
        userHandler = new UserHandler();
        roleHandler = new RoleHandler();
        permGroupHandler = new PermissionGroupHandler();
        try {
            System.out.println("\n======================================");
            System.out.println("Begin - Test Starting!");
        	System.out.println("Initialize LDAP : Start");
            startTime = System.currentTimeMillis();
            //delete all permgroups, other than predefined
            initializePermGroups();
            //delete all roles, other than predefined
            initializeRoles();
            //delete all users, other than predefined
            initializeUsers();
            endTime = System.currentTimeMillis();
            System.out.println("Initialize LDAP : End");
            System.out.println("Time for Initialize = " + (endTime - startTime) + " milliseconds");
        } catch (final LDAPException exception) {
            Assert.fail("Failed to wipe LDAP clean " + exception.getMessage());
        }
    }

    /**
     * Create-Read-Update-Read-Delete one permission group
     */
    //@Test
    public void testManageOnePermissionGroup() {
        System.out.println("\nManage 1 permission Group : Start");
        startTime = System.currentTimeMillis();
        createOnePermissionGroup();
        endTime = System.currentTimeMillis();
        System.out.println("Create = " + (endTime - startTime) + " milliseconds");

        startTime = System.currentTimeMillis();
        retrieveOnePermissionGroup();
        endTime = System.currentTimeMillis();
        System.out.println("Retrieve = " + (endTime - startTime) + " milliseconds");

        startTime = System.currentTimeMillis();
        modifyOnePermissionGroup();
        endTime = System.currentTimeMillis();
        System.out.println("Modify = " + (endTime - startTime) + " milliseconds");

        startTime = System.currentTimeMillis();
        retrieveOnePermissionGroup();
        endTime = System.currentTimeMillis();
        System.out.println("Retrieve = " + (endTime - startTime) + " milliseconds");

        startTime = System.currentTimeMillis();
        deleteOnePermissionGroup();
        endTime = System.currentTimeMillis();
        System.out.println("Delete = " + (endTime - startTime) + " milliseconds");
        System.out.println("Manage 1 permission Group : End");

    }

    /**
     * Create-Read-Update-Read-Delete 100 permission groups
     */
    //@Test
    public void testManage100PermissionGroups() {
        System.out.println("\nManage 100 permission Group : Start");
        System.out.print("Create ");
        startTime = System.currentTimeMillis();
        create100PermissionGroups();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.print("Retrieve ");
        startTime = System.currentTimeMillis();
        retrieveAllPermissionGroups();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.print("Modify ");
        startTime = System.currentTimeMillis();
        modify100PermissionGroups();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.print("Retrieve ");
        startTime = System.currentTimeMillis();
        retrieveAllPermissionGroups();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.print("Delete ");
        startTime = System.currentTimeMillis();
        delete100PermissionGroups();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");
        System.out.println("Manage 100 permission Group : End");
    }

    /**
     * Create-Read-Update-Read-Delete one role
     *
     */
    //@Test
    public void testManageOneRole() {
        System.out.println("\nManage 1 Role : Start");

        // needed for role
        createOnePermissionGroup();

        startTime = System.currentTimeMillis();
        createOneRole();
        endTime = System.currentTimeMillis();
        System.out.println("createOneRole = " + (endTime - startTime) + " milliseconds");

        startTime = System.currentTimeMillis();
        retrieveOneRole();
        endTime = System.currentTimeMillis();
        System.out.println("retrieveOneRole = " + (endTime - startTime) + " milliseconds");

        startTime = System.currentTimeMillis();
        modifyOneRole();
        endTime = System.currentTimeMillis();
        System.out.println("modifyOneRole = " + (endTime - startTime) + " milliseconds");

        startTime = System.currentTimeMillis();
        retrieveOneRole();
        endTime = System.currentTimeMillis();
        System.out.println("retrieveOneRole = " + (endTime - startTime) + " milliseconds");

        startTime = System.currentTimeMillis();
        deleteOneRole();
        endTime = System.currentTimeMillis();
        System.out.println("deleteOneRole = " + (endTime - startTime) + " milliseconds");

        System.out.println("Manage 1 role : End");
    }

    /**
    * Create-Read-Update-Read-Delete 100 roles
    *
    */
    //@Test
    public void testManage100Role() {
        System.out.println("\nManage 100 Role : Start");

        System.out.print("Create 100 PG ");
        startTime = System.currentTimeMillis();
        create100PermissionGroups();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.print("Create 100 Roles ");
        startTime = System.currentTimeMillis();
        create100Roles();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.print("Retrieve All Roles ");
        startTime = System.currentTimeMillis();
        retrieveAllRoles();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.print("Modify 100 Roles ");
        startTime = System.currentTimeMillis();
        modify100Roles();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.print("Delete 100 Roles ");
        startTime = System.currentTimeMillis();
        delete100Roles();
        endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " milliseconds");

        System.out.println("Manage 100 Role : End");
    }

    /**
    * Create-Read-Update-Read-Delete one user
    *
    */
    //@Test
	public void testManageOneUser(){
		System.out.println("\nManage 1 user : Start");

		createOnePermissionGroup();
		createOneRole();

		long startTime = System.currentTimeMillis();
		createOneUser();
		long endTime = System.currentTimeMillis();
		System.out.println("Create = " + (endTime - startTime) + " milliseconds" );

		startTime = System.currentTimeMillis();
		retrieveOneUser();
		endTime = System.currentTimeMillis();
		System.out.println("Retrieve = " + (endTime - startTime) + " milliseconds" );

		startTime = System.currentTimeMillis();
		modifyOneUser();
		endTime = System.currentTimeMillis();
		System.out.println("Modify = " + (endTime - startTime) + " milliseconds" );

		startTime = System.currentTimeMillis();
		retrieveOneUser();
		endTime = System.currentTimeMillis();
		System.out.println("Retrieve = " + (endTime - startTime) + " milliseconds" );

		startTime = System.currentTimeMillis();
		deleteOneUser();
		endTime = System.currentTimeMillis();
		System.out.println("Delete = " + (endTime - startTime) + " milliseconds");
		System.out.println("Manage 1 user : End");

	}

	/**
     * Create-Read-Update-Read-Delete 100 users
     *
     */
	//@Test
	public void testManage100Users(){
		System.out.println("\nManage 100 user : Start");

		System.out.print("Create 100 PG ");
	    startTime = System.currentTimeMillis();
	    create100PermissionGroups();
	    endTime = System.currentTimeMillis();
	    System.out.println((endTime - startTime) + " milliseconds");

	    System.out.print("Create 100 Roles ");
	    startTime = System.currentTimeMillis();
	    create100Roles();
	    endTime = System.currentTimeMillis();
	    System.out.println((endTime - startTime) + " milliseconds");

		System.out.print("Create 100 Users ");
		long startTime = System.currentTimeMillis();
		create100Users();
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) + " milliseconds" );

		System.out.print("Retrieve 100 Users ");
		startTime = System.currentTimeMillis();
		retrieveAllUsers();
		endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) + " milliseconds" );

		System.out.print("Modify 100 Users ");
		startTime = System.currentTimeMillis();
		modify100Users();
		endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) + " milliseconds" );

		System.out.print("Retrieve 100 Users ");
		startTime = System.currentTimeMillis();
		retrieveAllUsers();
		endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) + " milliseconds" );

		System.out.print("Delete 100 Users ");
		startTime = System.currentTimeMillis();
		delete100Users();
		endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) + " milliseconds" );
		System.out.println("Manage 100 user : End");
	}

    //@After
    public void completed() {
    	try {
            System.out.println("\nResetting LDAP : Start");
            startTime = System.currentTimeMillis();
            //delete all permgroups, other than predefined
            initializePermGroups();
            //delete all roles, other than predefined
            initializeRoles();
            //delete all users, other than predefined
            initializeUsers();
            endTime = System.currentTimeMillis();
            System.out.println("Resetting LDAP : End");
        } catch (final LDAPException exception) {
            Assert.fail("Failed to wipe LDAP clean " + exception.getMessage());
        }
        System.out.println("After - Test Finished!");
        System.out.println("\n======================================");
    }

    private void initializePermGroups() throws LDAPException {
        final List<IValueObject> permGroups = permGroupHandler.findAll(getLoginVO());
        for (final IValueObject permGroup : permGroups) {
            final PermissionGroupVO permGroupVo = (PermissionGroupVO) permGroup;
            if (!permGroupVo.isPredefined()) {
                permGroupHandler.delete(getLoginVO(), permGroupVo);
            }
        }
    }

    private void initializeRoles() throws LDAPException {
        final List<IValueObject> roles = roleHandler.findAll(getLoginVO());
        for (final IValueObject role : roles) {
            final RoleVO roleVo = (RoleVO) role;
            if (!roleVo.isPredefined()) {
                roleHandler.delete(getLoginVO(), roleVo);
            }
        }
    }

    private void initializeUsers() throws LDAPException {
        final List<IValueObject> users = userHandler.findAll(getLoginVO());
        for (final IValueObject user : users) {
            final UserVO userVo = (UserVO) user;
            if (!userVo.isPredefined()) {
                userHandler.delete(getLoginVO(), userVo);
            }
        }
    }

    public void createOneUser(){
		UserVO vo = new UserVO();
		vo.setUserId("user");
		vo.setPredefined(false);
		vo.setFname("user");
		vo.setLname("name");
		vo.setPassword("Ericsson1");
		final Set<String> role = new HashSet<String>();
        role.add("role");
		vo.setRoles(role);
		vo.setEmail("tester@test.com");
		vo.setPhone("9898989898");
		vo.setOrg("PDUOSS");
		try {
			userHandler.create(getLoginVO(), vo);
		} catch (LDAPException e) {
			Assert.fail("Error while creating user");
		}
	}

	public void retrieveOneUser(){
		UserVO vo = new UserVO();
		vo.setUserId("user");
		vo.setPredefined(false);
		try {
			userHandler.findById(getLoginVO(), vo);
		} catch (LDAPException e) {
			Assert.fail("Error while retrieving user");
		}
	}

	public void modifyOneUser(){
		UserVO vo = new UserVO();
		vo.setUserId("user");
		vo.setPredefined(false);
		vo.setFname("user1");
		vo.setLname("name1");
		vo.setPassword("UNCHANGED");
		final Set<String> role = new HashSet<String>();
        role.add("role");
		vo.setRoles(role);
		vo.setEmail("tester@tested.com");
		vo.setPhone("");
		vo.setOrg("");
		try {
			userHandler.modify(getLoginVO(), vo);
		} catch (LDAPException e) {
			Assert.fail("Error while modifying user");
		}
	}

	public void deleteOneUser(){
		UserVO vo = new UserVO();
		vo.setUserId("user");
		vo.setPredefined(false);
		try {
			userHandler.delete(getLoginVO(), vo);
		} catch (LDAPException e) {
			Assert.fail("Error while deleting user");
		}
	}

	public void create100Users(){
		System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }
            UserVO vo = new UserVO();
			vo.setUserId("user" + i);
			vo.setPredefined(false);
			vo.setFname("user" + i);
			vo.setLname("name" + i);
			vo.setPassword("Ericsson1");
			vo.setRoles(getRoles(i));
			vo.setEmail("tester@test.com");
			vo.setPhone("9898989898");
			vo.setOrg("PDUOSS");
			try {
				userHandler.create(getLoginVO(), vo);
			} catch (LDAPException e) {
				Assert.fail("Error while creating user " + i);
			}
		}
        System.out.print("]");
	}

	public void retrieveAllUsers(){
		try {
			userHandler.findAll(getLoginVO());
		} catch (LDAPException e) {
			Assert.fail("Error while retrieving all users");
		}
	}

	public void modify100Users(){
		System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }
            UserVO vo = new UserVO();
			vo.setUserId("user" + i);
			vo.setPredefined(false);
			vo.setFname("user" + (i+1));
			vo.setLname("name" + (i+1));
			vo.setPassword("UNCHANGED");
			vo.setRoles(getRoles(i));
			vo.setEmail("tester@tested.com");
			vo.setPhone("");
			vo.setOrg("");
			try {
				userHandler.modify(getLoginVO(), vo);
			} catch (LDAPException e) {
				Assert.fail("Error while modifying user " + i);
			}
		}
        System.out.print("]");
	}

	public void delete100Users(){
		System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }
			UserVO vo = new UserVO();
			vo.setUserId("user" + i);
			vo.setPredefined(false);
			try {
				userHandler.delete(getLoginVO(), vo);
			} catch (LDAPException e) {
				Assert.fail("Error while deleting user " + i);
			}
		}
        System.out.print("]");
	}

	private Set<String> getRoles(final int i) {
		final Set<String> roles = new HashSet<String>();
		roles.add("role"+ i);
		if (i<=97) {
			roles.add("role"+ (i+1));
			roles.add("role"+ (i+2));
		} else {
			roles.add("role"+ (i-1));
			roles.add("role"+ (i-2));
		}
		return roles;
	}

	public void createOneRole() {
        final RoleVO roleVO = new RoleVO();

        final Set<String> permGroup = new HashSet<String>();
        permGroup.add("permGroup");

        roleVO.setPermissionGroups(permGroup);
        roleVO.setRoleName("role");
        roleVO.setTitle("role_title");
        roleVO.setDescription("role_description");
        roleVO.setRemarks("role_remarks");
        roleVO.setPredefined(false);

        try {
            roleHandler.create(getLoginVO(), roleVO);
        } catch (final LDAPException le) {
            Assert.fail("Error while creating role");
        }
    }

    public void retrieveOneRole() {
        final RoleVO vo = new RoleVO();
        vo.setRoleName("role");
        vo.setPredefined(false);
        try {
            roleHandler.findById(getLoginVO(), vo);
        } catch (final LDAPException e) {
            Assert.fail("Error while retrieving role");
        }
    }

    public void modifyOneRole() {
        final RoleVO vo = new RoleVO();
        vo.setRoleName("role");
        vo.setPredefined(false);
        vo.setDescription("this is a modified role ");
        vo.setTitle("performance role");
        final Set<String> permGroup = new HashSet<String>();
        permGroup.add("permGroup");
        vo.setPermissionGroups(permGroup);
        try {
            roleHandler.modify(getLoginVO(), vo);
        } catch (final LDAPException e) {
            Assert.fail("Error while modifying role");
        }
    }

    public void deleteOneRole() {
        final RoleVO vo = new RoleVO();
        vo.setRoleName("role");
        vo.setPredefined(false);
        try {
            roleHandler.delete(getLoginVO(), vo);
        } catch (final LDAPException e) {
            Assert.fail("Error while deleting role");
        }
    }

    public void create100Roles() {
        System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }
            final RoleVO roleVO = new RoleVO();
            final Set<String> permGroup = new HashSet<String>();
            permGroup.add("permGroup" + i);

            roleVO.setPermissionGroups(permGroup);
            roleVO.setRoleName("role" + i);
            roleVO.setTitle("role_title" + i);
            roleVO.setDescription("role_description" + i);
            roleVO.setRemarks("role_remarks" + i);
            roleVO.setPredefined(false);

            try {
                roleHandler.create(getLoginVO(), roleVO);
            } catch (final LDAPException e) {
                Assert.fail("Error while creating role " + i);
            }
        }
        System.out.print("]");
    }

    public void retrieveAllRoles() {
        try {
            roleHandler.findAll(getLoginVO());
        } catch (final LDAPException e) {
            Assert.fail("Error while retrieving all roles");
        }
    }

    public void modify100Roles() {
        System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }

            final RoleVO roleVO = new RoleVO();
            final Set<String> permGroup = new HashSet<String>();
            permGroup.add("permGroup" + i + 1);

            roleVO.setPermissionGroups(permGroup);
            roleVO.setRoleName("role" + i);
            roleVO.setTitle("role_title" + i + 1);
            roleVO.setDescription("role_description" + i + 1);
            roleVO.setRemarks("role_remarks" + i + 1);
            roleVO.setPredefined(false);

            try {
                roleHandler.modify(getLoginVO(), roleVO);
            } catch (final LDAPException e) {
                Assert.fail("Error while modifying role " + i);
            }
        }
        System.out.print("]");
    }

    public void delete100Roles() {
        System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }

            final RoleVO roleVO = new RoleVO();
            roleVO.setRoleName("role" + i);
            roleVO.setPredefined(false);

            try {
                roleHandler.delete(getLoginVO(), roleVO);
            } catch (final LDAPException e) {
                Assert.fail("Error while deleting role " + i);
            }
        }
        System.out.print("]");
    }

    public void createOnePermissionGroup() {
        final PermissionGroupVO vo = new PermissionGroupVO();
        vo.setPermissionGroupName("permGroup");
        vo.setPredefined(false);
        vo.setDescription("this is permission group ");
        vo.setTitle("performance permgroups");
        vo.setPermissions(getPermissions(10));
        try {
            permGroupHandler.create(getLoginVO(), vo);
        } catch (final LDAPException e) {
            Assert.fail("Error while creating permission group");
        }
    }

    public void retrieveOnePermissionGroup() {
        final PermissionGroupVO vo = new PermissionGroupVO();
        vo.setPermissionGroupName("permGroup");
        vo.setPredefined(false);
        try {
            permGroupHandler.findById(getLoginVO(), vo);
        } catch (final LDAPException e) {
            Assert.fail("Error while retrieving permission group");
        }
    }

    public void modifyOnePermissionGroup() {
        final PermissionGroupVO vo = new PermissionGroupVO();
        vo.setPermissionGroupName("permGroup");
        vo.setPredefined(false);
        //description is modified
        vo.setDescription("this is a modified permission group ");
        vo.setTitle("performance permgroups");
        //permissions are modified
        vo.setPermissions(getPermissions(20));
        try {
            permGroupHandler.modify(getLoginVO(), vo);
        } catch (final LDAPException e) {
            Assert.fail("Error while modifying permission group");
        }
    }

    public void deleteOnePermissionGroup() {
        final PermissionGroupVO vo = new PermissionGroupVO();
        vo.setPermissionGroupName("permGroup");
        vo.setPredefined(false);
        try {
            permGroupHandler.delete(getLoginVO(), vo);
        } catch (final LDAPException e) {
            Assert.fail("Error while deleting permission group");
        }
    }

    public void create100PermissionGroups() {
        System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }
            final PermissionGroupVO vo = new PermissionGroupVO();
            vo.setPermissionGroupName("permGroup" + i);
            vo.setPredefined(false);
            vo.setDescription("this is permission group " + i);
            vo.setTitle("performance permgroups");
            vo.setPermissions(getPermissions(i));
            try {
                permGroupHandler.create(getLoginVO(), vo);
            } catch (final LDAPException e) {
                Assert.fail("Error while creating permission group " + i);
            }
        }
        System.out.print("]");
    }

    public void retrieveAllPermissionGroups() {
        try {
            permGroupHandler.findAll(getLoginVO());
        } catch (final LDAPException e) {
            Assert.fail("Error while retrieving all permission groups");
        }
    }

    public void modify100PermissionGroups() {
        System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }
            final PermissionGroupVO vo = new PermissionGroupVO();
            vo.setPermissionGroupName("permGroup" + i);
            vo.setPredefined(false);
            vo.setDescription("this is permission group " + i);
            //title is modified
            vo.setTitle("modified performance permgroups");
            // permissions are modified
            vo.setPermissions(getPermissions(i + 1));
            try {
                permGroupHandler.modify(getLoginVO(), vo);
            } catch (final LDAPException e) {
                Assert.fail("Error while modifying permission group " + i);
            }
        }
        System.out.print("]");
    }

    public void delete100PermissionGroups() {
        System.out.print("[");
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                System.out.print("*");
            }
            final PermissionGroupVO vo = new PermissionGroupVO();
            vo.setPermissionGroupName("permGroup" + i);
            vo.setPredefined(false);
            try {
                permGroupHandler.delete(getLoginVO(), vo);
            } catch (final LDAPException e) {
                Assert.fail("Error while deleting permission group " + i);
            }
        }
        System.out.print("]");
    }

    private Set<String> getPermissions(final int i) {
        final Set<String> perms = new HashSet<String>();
        perms.add(predefinedPerms.get(i % 4));
        perms.add(predefinedPerms.get(i % 3));
        return perms;
    }

    private LoginVO getLoginVO() {
        final LoginVO loginVo = new LoginVO();
        loginVo.setLoginId("admin");
        loginVo.setPassword("admin");
        return loginVo;
    }

}
