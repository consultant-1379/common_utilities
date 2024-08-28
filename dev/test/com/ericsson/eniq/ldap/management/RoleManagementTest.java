package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.COMMON_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.OBJECT_CLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONGROUP_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONS_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSION_GROUP;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UNIQUE_MEMBER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERPROFILES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USER_PROFILE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.LEFT_BRACE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_AND;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_COMMA;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_EQUALS;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_OR;
import static com.ericsson.eniq.ldap.util.LDAPConstants.RIGHT_BRACE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.common.testutilities.BaseUnitTestX;
import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.Role;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.vo.RoleVO;

public class RoleManagementTest extends BaseUnitTestX {

	private static final String ONLY_LDAP_EXCEPTION_MUST_BE_THROWN = "Only LDAP Exception must be thrown";
	private RoleManagement roleMgt;
	private DirContext mockedContext;
	private NamingEnumeration<SearchResult> mockedNamingEnumeration;
	private SearchResult mockedSearchResult;
	private Attributes mockedAttributes;
	private Attribute mockedAttribute;

	private String id;
	private String role;
	private String roleFilterLong;
	private String roleFilterShort;

	/**
	 * returns a filter: e.g. (%(objectclass=role)((cn=poweruser)))
	 * 
	 * @param id
	 * @return permission group filter
	 */
	private String getFilterLong(final String attribute, final String id) {
		final StringBuffer filter = new StringBuffer();
		filter.append(LEFT_BRACE);
		filter.append(OP_AND);
		filter.append(LEFT_BRACE);
		filter.append(OBJECT_CLASS);
		filter.append(OP_EQUALS);
		filter.append(attribute);
		filter.append(RIGHT_BRACE);
		filter.append(LEFT_BRACE);
		filter.append(LEFT_BRACE);
		filter.append(COMMON_NAME);
		filter.append(OP_EQUALS);
		filter.append(id);
		filter.append(RIGHT_BRACE);
		filter.append(RIGHT_BRACE);
		filter.append(RIGHT_BRACE);
		return filter.toString();
	}

	/**
	 * Returns a filter: e.g. (objectclass=role)
	 * 
	 * @return permission group filter
	 */
	private String getFilterShort(final String attribute) {
		final StringBuilder filter = new StringBuilder();
		filter.append(LEFT_BRACE);
		filter.append(OBJECT_CLASS);
		filter.append(OP_EQUALS);
		filter.append(attribute);
		filter.append(RIGHT_BRACE);
		return filter.toString();
	}

	/**
	 * returns a filter: e.g.
	 * (%(objectclass=role)(|(cn=networkgroup)(cn=terminalgroup)))
	 * 
	 * @param id
	 * @return permission group filter
	 */
	private String getMulitFilterLong(final String attribute, final List<String> subAttributes) {
		final StringBuffer filter = new StringBuffer();
		filter.append(LEFT_BRACE);
		filter.append(OP_AND);
		filter.append(LEFT_BRACE);
		filter.append(OBJECT_CLASS);
		filter.append(OP_EQUALS);
		filter.append(attribute);
		filter.append(RIGHT_BRACE);
		filter.append(LEFT_BRACE);
		filter.append(OP_OR);

		for (final String att : subAttributes) {
			filter.append(LEFT_BRACE);
			filter.append(COMMON_NAME);
			filter.append(OP_EQUALS);
			filter.append(att);
			filter.append(RIGHT_BRACE);
		}

		filter.append(RIGHT_BRACE);
		filter.append(RIGHT_BRACE);
		return filter.toString();
	}

	@Before
	public void setup() {
		recreateMockeryContext();
		roleMgt = new RoleManagement();
		id = "poweruser";
		role = ROLES_BASE_DN;
		roleFilterLong = getFilterLong(ROLE, id);
		roleFilterShort = getFilterShort(ROLE);

		mockedContext = context.mock(DirContext.class);
		mockedNamingEnumeration = context.mock(NamingEnumeration.class);
		mockedSearchResult = context.mock(SearchResult.class);
		mockedAttributes = context.mock(Attributes.class);
		mockedAttribute = context.mock(Attribute.class);
	}

	@Test(expected = LDAPException.class)
	public void testCreateInvalidSinglePermissionGroup() throws NamingException, LDAPException {
		final String newrole = "role1";
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
						with(any(SearchControls.class)));
			}
		});
		final MESSAGES result = roleMgt.create(roleLDAP, mockedContext);
	}

	@Test(expected = LDAPException.class)
	public void testCreateInvalidMultiplePermissionGroup() throws NamingException, LDAPException {
		final String newrole = "role1";
		final String newPermissionGroup1 = "newPermissionGroup1";
		final String newPermissionGroup2 = "newPermissionGroup2";

		final List<String> permissionGroupsList = new ArrayList<String>();
		permissionGroupsList.add(newPermissionGroup1);
		permissionGroupsList.add(newPermissionGroup2);

		final String permissionGroupFilterLong = getMulitFilterLong(PERMISSION_GROUP, permissionGroupsList);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup2);
		permissionGroups.add(newPermissionGroup1);

		// Create LDAP Object
		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
						with(any(SearchControls.class)));
			}
		});
		final MESSAGES result = roleMgt.create(roleLDAP, mockedContext);
	}

	@Test
	public void testCreateSinglePermissionGroupFail() throws NamingException, LDAPException {
		final String newrole = "role1";
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
							with(any(SearchControls.class)));
				}
			});

			final MESSAGES result = roleMgt.create(roleLDAP, mockedContext);

			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_SELECTED_PERMGROUPS_DO_NOT_EXIST.getMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testCreateSinglePermissionGroupPass() throws NamingException, LDAPException {
		final String newrole = "role1";
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		final RoleManagementStub roleManagementStub = new RoleManagementStub(true);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
						with(any(SearchControls.class)));
				allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
			}
		});

		final MESSAGES result = roleManagementStub.create(roleLDAP, mockedContext);
		Assert.assertEquals(MESSAGES.MSG_SUCCESS, result);
	}

	@Test
	public void testCreateMultiPermissionGroupPass() throws NamingException, LDAPException {
		final String newrole = "role1";
		final String newPermissionGroup1 = "newPermissionGroup1";
		final String newPermissionGroup2 = "newPermissionGroup2";

		final List<String> permissionGroupsList = new ArrayList<String>();
		permissionGroupsList.add(newPermissionGroup1);
		permissionGroupsList.add(newPermissionGroup2);

		final String permissionGroupFilterLong = getMulitFilterLong(PERMISSION_GROUP, permissionGroupsList);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup2);
		permissionGroups.add(newPermissionGroup1);

		// Create LDAP Object
		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		final RoleManagementStub roleManagementStub = new RoleManagementStub(true);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
						with(any(SearchControls.class)));
				allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
			}
		});

		final MESSAGES result = roleManagementStub.create(roleLDAP, mockedContext);
		Assert.assertEquals(MESSAGES.MSG_SUCCESS, result);
	}

	@Test
	public void testCreateDirectoryServerAlreadyExists() {
		final String newrole = "role1";
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
							with(any(SearchControls.class)));
					will(throwException(new NameAlreadyBoundException()));
				}
			});

			final MESSAGES result = roleMgt.create(roleLDAP, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");

		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testCreatePermissionGroupDoNotExists() {
		final String newrole = "role1";
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
							with(any(SearchControls.class)));
				}
			});

			final MESSAGES result = roleMgt.create(roleLDAP, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");

		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_SELECTED_PERMGROUPS_DO_NOT_EXIST.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testCreateGroupAlreadyExists() {
		final String newrole = "role1";
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		final RoleManagementStub roleManagementStub = new RoleManagementStub(true);
		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
							with(any(SearchControls.class)));
					allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
					will(throwException(new NameAlreadyBoundException()));
				}
			});

			final MESSAGES result = roleManagementStub.create(roleLDAP, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");

		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_ROLE_ALREADY_EXISTS.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testCreateUnexpectedExceptionExists() {
		final String newrole = "role1";
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(newrole);
		vo.setPermissionGroups(permissionGroups);

		final Role roleLDAP = new Role(vo);

		final RoleManagementStub roleManagementStub = new RoleManagementStub(true);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionGroupFilterLong)),
							with(any(SearchControls.class)));
					allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
					will(throwException(new Exception()));
				}
			});

			final MESSAGES result = roleManagementStub.create(roleLDAP, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");

		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testFindAll() {

		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(role)), with(equal(roleFilterShort)), with(any(SearchControls.class)));
					will(returnValue(mockedNamingEnumeration));
					one(mockedNamingEnumeration).hasMore();
					will(returnValue(true));
					one(mockedNamingEnumeration).hasMore();
					will(returnValue(false));
					one(mockedNamingEnumeration).next();
					will(returnValue(mockedSearchResult));
					one(mockedSearchResult).getAttributes();
					will(returnValue(mockedAttributes));
				}
			});
			final List<ILDAPObject> result = roleMgt.findAll(mockedContext);
			Assert.assertTrue("Only one role must be present", result.size() == 1);
			Assert.assertEquals(mockedAttributes, result.get(0).getAttributes(""));
		} catch (final LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testFindById() throws LDAPException, NamingException {

		try {
			context.checking(new Expectations() {

				{
					one(mockedContext).search(with(equal(role)), with(equal(roleFilterLong)), with(any(SearchControls.class)));
					will(returnValue(mockedNamingEnumeration));
					one(mockedNamingEnumeration).hasMore();
					will(returnValue(true));
					one(mockedNamingEnumeration).hasMore();
					will(returnValue(false));
					one(mockedNamingEnumeration).next();
					will(returnValue(mockedSearchResult));
					one(mockedSearchResult).getAttributes();
					will(returnValue(mockedAttributes));
				}
			});

			final ILDAPObject result = roleMgt.findById(id, mockedContext);
			Assert.assertEquals(mockedAttributes, result.getAttributes(""));
		} catch (final LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (final NamingException e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test(expected = LDAPException.class)
	public void testFindByIdLDAPExceptionByNamingException() throws LDAPException, NamingException {

		context.checking(new Expectations() {

			{
				one(mockedContext).search(with(equal(role)), with(equal(roleFilterLong)), with(any(SearchControls.class)));
				will(throwException(new NamingException()));
			}
		});

		roleMgt.findById(id, mockedContext);

	}

	@Test(expected = LDAPException.class)
	public void testFindByIdLDAPExceptionByException() throws LDAPException, NamingException {
		context.checking(new Expectations() {
			{
				one(mockedContext).search(with(equal(role)), with(equal(roleFilterLong)), with(any(SearchControls.class)));
				will(throwException(new Exception()));
			}
		});

		roleMgt.findById(id, mockedContext);
	}

	@Test
	public void testIsAllPermissionsValidSinglePermissionGroupFail() throws NamingException, LDAPException {
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
						with(any(SearchControls.class)));
				allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
			}
		});

		final boolean result = roleMgt.isAllPermissionGroupsValid(permissionGroups, mockedContext);
		Assert.assertFalse(result);
	}

	@Test
	public void testIsAllPermissionGroupsValidDirectoryException() throws NamingException, LDAPException {
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
							with(any(SearchControls.class)));
					will(throwException(new NameAlreadyBoundException()));
					allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
				}
			});

			final boolean result = roleMgt.isAllPermissionGroupsValid(permissionGroups, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testIsAllPermissionGroupsValidUnexpectedException() throws NamingException, LDAPException {
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, newPermissionGroup);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
							with(any(SearchControls.class)));
					will(throwException(new Exception()));
					allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
				}
			});

			final boolean result = roleMgt.isAllPermissionGroupsValid(permissionGroups, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testIsAllPermissionGroupsValidMultiPermissionFail() throws NamingException, LDAPException {
		final String newPermissionGroup1 = "newPermissionGroup1";
		final String newPermissionGroup2 = "newPermissionGroup2";

		final List<String> permissionGroupList = new ArrayList<String>();
		permissionGroupList.add(newPermissionGroup1);
		permissionGroupList.add(newPermissionGroup2);
		final String permissionGroupFilterLong = getMulitFilterLong(PERMISSION_GROUP, permissionGroupList);

		final Set<String> permissionGroups = new HashSet<String>();
		permissionGroups.add(newPermissionGroup2);
		permissionGroups.add(newPermissionGroup1);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionGroupFilterLong)),
						with(any(SearchControls.class)));
				allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
			}
		});

		final boolean result = roleMgt.isAllPermissionGroupsValid(permissionGroups, mockedContext);
		Assert.assertFalse(result);
	}

	@Test(expected = LDAPException.class)
	public void testFindAllNamingException() throws NamingException, LDAPException {
		final String newPermissionGroup = "newPermissionGroup";
		final String permissionGroupFilterShort = getFilterShort(PERMISSION_GROUP);

		context.checking(new Expectations() {
			{
				one(mockedContext).search(with(equal(role)), with(equal(roleFilterShort)), with(any(SearchControls.class)));
				will(throwException(new NamingException("naming")));
			}
		});
		final List<ILDAPObject> result = roleMgt.findAll(mockedContext);
	}

	@Test(expected = LDAPException.class)
	public void testFindNullPointerException() throws NamingException, LDAPException {

		context.checking(new Expectations() {
			{
				one(mockedContext).search(with(equal(role)), with(equal(roleFilterShort)), with(any(SearchControls.class)));
				will(throwException(new NullPointerException("null")));
			}
		});
		final List<ILDAPObject> result = roleMgt.findAll(mockedContext);
	}

	@Test
	public void testFindAllDirectoryException() {
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(role)), with(equal(roleFilterShort)), with(any(SearchControls.class)));
					will(throwException(new NamingException("naming")));
				}
			});
			final List<ILDAPObject> result = roleMgt.findAll(mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testFindAllLDAPException() {
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(role)), with(equal(roleFilterShort)), with(any(SearchControls.class)));
					will(throwException(new NullPointerException("null")));
				}
			});
			final List<ILDAPObject> result = roleMgt.findAll(mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test(expected = LDAPException.class)
	public void testFindAllSearchExceptionHandlingExpectException() throws NamingException, LDAPException {
		context.checking(new Expectations() {
			{
				one(mockedContext).search(with(equal(role)), with(equal(roleFilterShort)), with(any(SearchControls.class)));
				will(throwException(new NamingException("naming")));
			}
		});
		final List<ILDAPObject> result = roleMgt.findAll(mockedContext);
	}

	@Test
	public void testFindAllUnexpectedException() {
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(role)), with(equal(roleFilterShort)), with(any(SearchControls.class)));
					will(throwException(new NullPointerException("null")));
				}
			});
			final List<ILDAPObject> result = roleMgt.findAll(mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}
	
	@Test
	public void testDeleteRoleSuccess() throws NamingException, LDAPException {
		final Role roleLDAP = getUserDefinedRoleObject();
		final String associatedUser = "user1";

		final RoleManagementStub roleManagementStub = new RoleManagementStub(true);
		
		context.checking(new Expectations() {
			{
				one(mockedContext).search(with(equal(USERPROFILES_BASE_DN)),
						with(equal(getUserProfileSearchFilter(getRoleDN(id)))),with(any(SearchControls.class)));
				will(returnValue(mockedNamingEnumeration));
				one(mockedNamingEnumeration).hasMore();
				will(returnValue(true));
				one(mockedNamingEnumeration).hasMore();
				will(returnValue(false));
				one(mockedNamingEnumeration).next();
				will(returnValue(mockedSearchResult));
				one(mockedSearchResult).getAttributes();
				will(returnValue(mockedAttributes));
				one(mockedAttributes).get(with(equal(UID)));
				will(returnValue(mockedAttribute));
				one(mockedAttribute).get();
				will(returnValue(associatedUser));
				one(mockedContext).unbind(with(equal(getRoleDN(id))));
			}
		});

		final MESSAGES result = roleManagementStub.delete(roleLDAP, mockedContext);
		Assert.assertEquals(MESSAGES.MSG_SUCCESS, result);
	}
	
	@Test
	public void testDeleteRoleFailureWhenRemovingReferences() throws Exception {
		final Role roleLDAP = getUserDefinedRoleObject();

		final RoleManagementStub roleManagementStub = new RoleManagementStub(true);

		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(USERPROFILES_BASE_DN)),
							with(equal(getUserProfileSearchFilter(getRoleDN(id)))),with(any(SearchControls.class)));
					will(throwException(new NamingException()));
				}
			});
			final MESSAGES result = roleManagementStub.delete(roleLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_FAILED_TO_DELETE_ROLES_FROM_USER_PROFILES.getMessage(), e.getErrorMessage());
		}
	}

	@Test
	public void testDeleteRoleFailureWhenUnbind() throws Exception {
		final Role roleLDAP = getUserDefinedRoleObject();

		final RoleManagementStub roleManagementStub = new RoleManagementStub(true);

		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(USERPROFILES_BASE_DN)),
							with(equal(getUserProfileSearchFilter(getRoleDN(id)))),with(any(SearchControls.class)));
					will(returnValue(mockedNamingEnumeration));
					one(mockedNamingEnumeration).hasMore();
					will(returnValue(false));
					one(mockedContext).unbind(with(equal(getRoleDN(id))));
					will(throwException(new NamingException()));
				}
			});
			final MESSAGES result = roleManagementStub.delete(roleLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage(), e.getErrorMessage());
		}
	}
	
	@Test
	public void testDeleteRoleFailureWhenUncheckedException() throws Exception {
		final Role roleLDAP = getUserDefinedRoleObject();

		final RoleManagementStub roleManagementStub = new RoleManagementStub(true);

		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(USERPROFILES_BASE_DN)),
							with(equal(getUserProfileSearchFilter(getRoleDN(id)))),with(any(SearchControls.class)));
					will(returnValue(mockedNamingEnumeration));
					one(mockedNamingEnumeration).hasMore();
					will(returnValue(false));
					one(mockedContext).unbind(with(equal(getRoleDN(id))));
					will(throwException(new NullPointerException()));
				}
			});
			final MESSAGES result = roleManagementStub.delete(roleLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage(), e.getErrorMessage());
		}
	}
	
	@Test
	public void testModifyRoleSuccess() throws Exception {
		final Role roleLDAP = getUserDefinedRoleObject();
        final String roleDN = getRoleDN(id);
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).rebind( with(equal(roleDN)),with(equal(roleLDAP)));
				}
			});
			final MESSAGES result = roleMgt.modify(roleLDAP, mockedContext);
			Assert.assertEquals(result,MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyRoleNamingException() throws Exception {
		final Role roleLDAP = getUserDefinedRoleObject();
        final String roleDN = getRoleDN(id);
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).rebind( with(equal(roleDN)),with(equal(roleLDAP)));
					will(throwException(new NamingException()));
				}
			});
			final MESSAGES result = roleMgt.modify(roleLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyRoleGeneralException() throws Exception {
		final Role roleLDAP = getUserDefinedRoleObject();
        final String roleDN = getRoleDN(id);
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).rebind( with(equal(roleDN)),with(equal(roleLDAP)));
					will(throwException(new NullPointerException()));
				}
			});
			final MESSAGES result = roleMgt.modify(roleLDAP, mockedContext);
			Assert.assertEquals(result,MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	private Role getPredefinedRoleObject() {
		final RoleVO vo = new RoleVO();
		vo.setPredefined(true);
		vo.setRoleName(id);
		final Role roleLDAP = new Role(vo);
		return roleLDAP;
	}
	
	private Role getUserDefinedRoleObject() {
		final RoleVO vo = new RoleVO();
		vo.setPredefined(false);
		vo.setRoleName(id);
		final Role roleLDAP = new Role(vo);
		return roleLDAP;
	}
	
	private String getRoleDN(final String roleName) {
		final StringBuffer roleDN = new StringBuffer();
		roleDN.append(COMMON_NAME);
		roleDN.append(OP_EQUALS);
		roleDN.append(roleName);
		roleDN.append(OP_COMMA);
		roleDN.append(ROLES_BASE_DN);
		return roleDN.toString();
	}

	private String getUserProfileSearchFilter(final String roleDN){
		final StringBuffer userProfileFilter = new StringBuffer();
		userProfileFilter.append(LEFT_BRACE);
		userProfileFilter.append(OP_AND);
		userProfileFilter.append(LEFT_BRACE);
		userProfileFilter.append(OBJECT_CLASS);
		userProfileFilter.append(OP_EQUALS);
		userProfileFilter.append(USER_PROFILE);
		userProfileFilter.append(RIGHT_BRACE);
		userProfileFilter.append(LEFT_BRACE);
		userProfileFilter.append(UNIQUE_MEMBER);
		userProfileFilter.append(OP_EQUALS);
		userProfileFilter.append(roleDN);
		userProfileFilter.append(RIGHT_BRACE);
		userProfileFilter.append(RIGHT_BRACE);
		return userProfileFilter.toString();
	}


	class RoleManagementStub extends RoleManagement {
		
		private final boolean isPermissionValid;

		protected RoleManagementStub(final boolean isPermissionValid) {
			this.isPermissionValid = isPermissionValid;
		}

		@Override
		protected boolean isAllPermissionGroupsValid(final Set<String> permissionGroups, final DirContext conn) throws LDAPException {
			return isPermissionValid;
		}
		
		@Override
		protected void removeUniqueMemberReferenceInUserProfile( final String userProfileDN, final String roleDN, final DirContext conn)
				throws NamingException {
			return;
		}
	}
}
