/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2011 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.COMMON_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.OBJECT_CLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSION;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONGROUP_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSIONS_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PERMISSION_GROUP;
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
import com.ericsson.eniq.ldap.entity.PermissionGroup;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;

/**
 * @author eemecoy
 * 
 */
public class PermissionGroupManagementTest extends BaseUnitTestX {

	private static final String ONLY_LDAP_EXCEPTION_MUST_BE_THROWN = "Only LDAP Exception must be thrown";
	private PermissionGroupManagement permissionGroupManagement;
	private DirContext mockedContext;
	private NamingEnumeration<SearchResult> mockedNamingEnumeration;
	private SearchResult mockedSearchResult;
	private Attributes mockedAttributes;
	private Attribute mockedAttribute;
	private String id;
	private String permissionGroup;
	private String permission;
	private String permissionGroupFilterLong;
	private String permissionGroupFilterShort;
	private final String PERMISSION_1 = "permission1";
	private final String PERMISSION_2 = "permission2";

	/**
	 * returns a filter: e.g.
	 * (%(objectclass=permissiongroup)((cn=rankinggroup)))
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
	 * returns a filter: e.g.
	 * (%(objectclass=permission)(|(cn=eventsui.network.view
	 * )(cn=eventsui.subscriber.view)))
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

	/**
	 * Returns a filter: e.g. (objectclass=permissiongroup)
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

	@Before
	public void setup() {
		recreateMockeryContext();
		permissionGroupManagement = new PermissionGroupManagement();
		id = "rankinggroup";
		permission = PERMISSIONS_BASE_DN;
		permissionGroup = PERMISSIONGROUP_BASE_DN;
		permissionGroupFilterLong = getFilterLong(PERMISSION_GROUP, id);
		permissionGroupFilterShort = getFilterShort(PERMISSION_GROUP);

		mockedContext = context.mock(DirContext.class);
		mockedNamingEnumeration = context.mock(NamingEnumeration.class);
		mockedSearchResult = context.mock(SearchResult.class);
		mockedAttributes = context.mock(Attributes.class);
		mockedAttribute = context.mock(Attribute.class);
	}

	@Test(expected = LDAPException.class)
	public void testCreateInvalidSinglePermission() throws NamingException, LDAPException {
		final String permGroupID = "newpermissiongroup";
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);
		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)), with(any(SearchControls.class)));
			}
		});
		final MESSAGES result = permissionGroupManagement.create(permissionGroupLDAP, mockedContext);
	}

	@Test(expected = LDAPException.class)
	public void testCreateInvalidMultiplePermission() throws NamingException, LDAPException {
		final String permGroupID = "newpermissiongroup";
		final String newPermission1 = "permission1";
		final String newPermission2 = "permission2";

		final List<String> permissionLst = new ArrayList<String>();
		permissionLst.add(newPermission2);
		permissionLst.add(newPermission1);

		final String permissionFilterLong = getMulitFilterLong(PERMISSION, permissionLst);

		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission1);
		permissions.add(newPermission2);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)), with(any(SearchControls.class)));
			}
		});
		final MESSAGES result = permissionGroupManagement.create(permissionGroupLDAP, mockedContext);
	}

	@Test
	public void testCreateSinglePermissionFail() throws NamingException, LDAPException {
		final String permGroupID = "newpermissiongroup";
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);
		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)),
							with(any(SearchControls.class)));
				}
			});

			final MESSAGES result = permissionGroupManagement.create(permissionGroupLDAP, mockedContext);

			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testCreateSinglePermissionPass() throws NamingException, LDAPException {
		final String permGroupID = "newpermissiongroup";
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);
		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		final PermissionGroupManagementStub permissionGroupManagementMock = new PermissionGroupManagementStub(null, true);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)), with(any(SearchControls.class)));
				allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
			}
		});

		final MESSAGES result = permissionGroupManagementMock.create(permissionGroupLDAP, mockedContext);
		Assert.assertEquals(MESSAGES.MSG_SUCCESS, result);
	}

	@Test
	public void testCreateMultiPermissionPass() throws NamingException, LDAPException {
		final String permGroupID = "newpermissiongroup";
		final String newPermission1 = "permission1";
		final String newPermission2 = "permission2";

		final List<String> permissionLst = new ArrayList<String>();
		permissionLst.add(newPermission2);
		permissionLst.add(newPermission1);

		final Set<String> permissionSet = new HashSet<String>();
		permissionLst.add(newPermission2);
		permissionLst.add(newPermission1);

		final String permissionFilterLong = getMulitFilterLong(PERMISSION, permissionLst);
		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission2);
		permissions.add(newPermission1);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		final PermissionGroupManagementStub permissionGroupManagementMock = new PermissionGroupManagementStub(null, true);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)), with(any(SearchControls.class)));
				allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
			}
		});

		final MESSAGES result = permissionGroupManagementMock.create(permissionGroupLDAP, mockedContext);
		Assert.assertEquals(MESSAGES.MSG_SUCCESS, result);
	}

	@Test
	public void testCreateDirectoryServerAlreadyExists() {

		final String permGroupID = "newpermissiongroup";
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);
		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)),
							with(any(SearchControls.class)));
					will(throwException(new NameAlreadyBoundException()));
				}
			});

			final MESSAGES result = permissionGroupManagement.create(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");

		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testCreatePermissionGroupDoNotExists() {

		final String permGroupID = "newpermissiongroup";
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);
		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)),
							with(any(SearchControls.class)));
				}
			});

			final MESSAGES result = permissionGroupManagement.create(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");

		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testCreateGroupAlreadyExists() {

		final String permGroupID = "newpermissiongroup";
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);
		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		final PermissionGroupManagementStub permissionGroupManagementMock = new PermissionGroupManagementStub(null, true);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)),
							with(any(SearchControls.class)));
					allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
					will(throwException(new NameAlreadyBoundException()));
				}
			});

			final MESSAGES result = permissionGroupManagementMock.create(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");

		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_PERMGROUP_ALREADY_EXISTS.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testCreateUnexpectedExceptionExists() {

		final String permGroupID = "newpermissiongroup";
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);
		// Create LDAP Object
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		vo.setPermissions(permissions);

		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		final PermissionGroupManagementStub permissionGroupManagementMock = new PermissionGroupManagementStub(null, true);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)),
							with(any(SearchControls.class)));
					allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
					will(throwException(new Exception()));
				}
			});

			final MESSAGES result = permissionGroupManagementMock.create(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");

		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testFindByIdEmptyAttributes() throws LDAPException, NamingException {

		final NamingEnumeration<SearchResult> mockedNamingEnumeration = context.mock(NamingEnumeration.class, "NamingEnumerationFindbyid");
		final Attributes mockedAttributes = context.mock(Attributes.class, "AttributesFindbyid");
		final SearchResult mockedSearchResult = context.mock(SearchResult.class, "mockedSearchResultFindbyid");
		context.checking(new Expectations() {

			{
				one(mockedContext).search(with(equal(permissionGroup)), with(equal(permissionGroupFilterLong)), with(any(SearchControls.class)));
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

		final ILDAPObject result = permissionGroupManagement.findById(id, mockedContext);
		Assert.assertEquals(mockedAttributes, result.getAttributes(""));
	}

	@Test(expected = LDAPException.class)
	public void testFindByIdLDAPExceptionByNamingException() throws LDAPException, NamingException {

		context.checking(new Expectations() {

			{
				one(mockedContext).search(with(equal(permissionGroup)), with(equal(permissionGroupFilterLong)), with(any(SearchControls.class)));
				will(throwException(new NamingException()));
			}
		});

		permissionGroupManagement.findById(id, mockedContext);

	}

	@Test(expected = LDAPException.class)
	public void testFindByIdLDAPExceptionByException() throws LDAPException, NamingException {

		context.checking(new Expectations() {

			{
				one(mockedContext).search(with(equal(permissionGroup)), with(equal(permissionGroupFilterLong)), with(any(SearchControls.class)));
				will(throwException(new Exception()));
			}
		});

		permissionGroupManagement.findById(id, mockedContext);

	}

	@Test
	public void testIsAllPermissionsValidSinglePermissionFail() throws NamingException, LDAPException {
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);

		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)), with(any(SearchControls.class)));
				allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
			}
		});

		final boolean result = permissionGroupManagement.isAllPermissionsValid(permissions, mockedContext);
		Assert.assertFalse(result);
	}

	@Test
	public void testIsAllPermissionsValidDirectoryException() throws NamingException, LDAPException {
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);

		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)),
							with(any(SearchControls.class)));
					will(throwException(new NameAlreadyBoundException()));
					allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
				}
			});

			final boolean result = permissionGroupManagement.isAllPermissionsValid(permissions, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testIsAllPermissionsValidUnexpectedException() throws NamingException, LDAPException {
		final String newPermission = "permission1";
		final String permissionFilterLong = getFilterLong(PERMISSION, newPermission);

		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission);

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)),
							with(any(SearchControls.class)));
					will(throwException(new Exception()));
					allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
				}
			});

			final boolean result = permissionGroupManagement.isAllPermissionsValid(permissions, mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testIsAllPermissionsValidMultiPermissionFail() throws NamingException, LDAPException {
		final String newPermission1 = "permission1";
		final String newPermission2 = "permission2";

		final List<String> permissionList = new ArrayList<String>();
		permissionList.add(newPermission2);
		permissionList.add(newPermission1);

		final String permissionFilterLong = getMulitFilterLong(PERMISSION, permissionList);

		final Set<String> permissions = new HashSet<String>();
		permissions.add(newPermission2);
		permissions.add(newPermission1);

		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(equal(PERMISSIONS_BASE_DN)), with(equal(permissionFilterLong)), with(any(SearchControls.class)));
				allowing(mockedContext).bind(with(any(String.class)), with(any(Attribute.class)));
			}
		});

		final boolean result = permissionGroupManagement.isAllPermissionsValid(permissions, mockedContext);
		Assert.assertFalse(result);
	}

	@Test(expected = LDAPException.class)
	public void testFindAllNamingException() throws NamingException, LDAPException {

		context.checking(new Expectations() {
			{
				one(mockedContext).search(with(equal(permissionGroup)), with(equal(permissionGroupFilterShort)), with(any(SearchControls.class)));
				will(throwException(new NamingException("naming")));
			}
		});
		final List<ILDAPObject> result = permissionGroupManagement.findAll(mockedContext);
	}

	@Test(expected = LDAPException.class)
	public void testFindNullPointerException() throws NamingException, LDAPException {

		context.checking(new Expectations() {
			{
				one(mockedContext).search(with(equal(permissionGroup)), with(equal(permissionGroupFilterShort)), with(any(SearchControls.class)));
				will(throwException(new NullPointerException("null")));
			}
		});
		final List<ILDAPObject> result = permissionGroupManagement.findAll(mockedContext);
	}

	@Test
	public void testFindAllDirectoryException() {
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(permissionGroup)), with(equal(permissionGroupFilterShort)), with(any(SearchControls.class)));
					will(throwException(new NamingException("naming")));
				}
			});
			final List<ILDAPObject> result = permissionGroupManagement.findAll(mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testFindAllUnexpectedException() {
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(permissionGroup)), with(equal(permissionGroupFilterShort)), with(any(SearchControls.class)));
					will(throwException(new NullPointerException("null")));
				}
			});
			final List<ILDAPObject> result = permissionGroupManagement.findAll(mockedContext);
			Assert.fail("Expected LDAP Exception is not thrown");
		} catch (final LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage(), e.getErrorMessage());
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	@Test
	public void testFindAll() {
		final String permissionFilterShort = getFilterShort(PERMISSION_GROUP);

		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).search(with(equal(PERMISSIONGROUP_BASE_DN)), with(equal(permissionFilterShort)),
							with(any(SearchControls.class)));
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
			final List<ILDAPObject> result = permissionGroupManagement.findAll(mockedContext);
			Assert.assertEquals(mockedAttributes, result.get(0).getAttributes(""));
		} catch (final LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (final Exception e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}
	
	@Test
	public void testDeletePermissionGroupSuccess() throws NamingException, LDAPException {
		final String permGroupID = "permissionGroup1";
		final String associatedRole = "role1";
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		final PermissionGroupManagementStub mockPermissionGroupManagement = new PermissionGroupManagementStub(mockedNamingEnumeration, true);

		context.checking(new Expectations() {
			{
				one(mockedNamingEnumeration).hasMore();
				will(returnValue(true));
				one(mockedNamingEnumeration).hasMore();
				will(returnValue(false));
				one(mockedNamingEnumeration).next();
				will(returnValue(mockedSearchResult));
				one(mockedSearchResult).getAttributes();
				will(returnValue(mockedAttributes));
				one(mockedAttributes).get(with(equal(COMMON_NAME)));
				will(returnValue(mockedAttribute));
				one(mockedAttribute).get();
				will(returnValue(associatedRole));
				one(mockedContext).unbind(with(equal(getPermGroupDN(permGroupID))));
			}
		});

		final MESSAGES result = mockPermissionGroupManagement.delete(permissionGroupLDAP, mockedContext);
		Assert.assertEquals(MESSAGES.MSG_SUCCESS, result);
	}
	
	@Test
	public void testDeletePermissionGroupFailureWhenRemovingReferences() throws Exception {
		final String permGroupID = "permissionGroup1";
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		final PermissionGroupManagementStub mockPermissionGroupManagement = new PermissionGroupManagementStub(mockedNamingEnumeration, true);

		try {
			context.checking(new Expectations() {
				{
					one(mockedNamingEnumeration).hasMore();
					will(throwException(new NamingException()));
				}
			});
			final MESSAGES result = mockPermissionGroupManagement.delete(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_FAILED_TO_DELETE_PERMGROUP_FROM_ROLES.getMessage(), e.getErrorMessage());
		}
	}

	@Test
	public void testDeletePermissionGroupFailureWhenUnbind() throws Exception {
		final String permGroupID = "permissionGroup1";
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		final PermissionGroupManagementStub mockPermissionGroupManagement = new PermissionGroupManagementStub(mockedNamingEnumeration, true);

		try {
			context.checking(new Expectations() {
				{
					one(mockedNamingEnumeration).hasMore();
					will(returnValue(false));
					one(mockedContext).unbind(with(equal(getPermGroupDN(permGroupID))));
					will(throwException(new NamingException()));
				}
			});
			final MESSAGES result = mockPermissionGroupManagement.delete(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage(), e.getErrorMessage());
		}
	}
	
	@Test
	public void testDeletePermissionGroupFailureWhenUncheckedException() throws Exception {
		final String permGroupID = "permissionGroup1";
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		final PermissionGroupManagementStub mockPermissionGroupManagement = new PermissionGroupManagementStub(mockedNamingEnumeration, true);

		try {
			context.checking(new Expectations() {
				{
					one(mockedNamingEnumeration).hasMore();
					will(returnValue(false));
					one(mockedContext).unbind(with(equal(getPermGroupDN(permGroupID))));
					will(throwException(new NullPointerException()));
				}
			});
			final MESSAGES result = mockPermissionGroupManagement.delete(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage(), e.getErrorMessage());
		}
	}
	

	@Test
	public void testModifyPermGroupSuccess() throws Exception {
		final String permGroupID = "permissionGroup1";
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).rebind(with(equal(getPermGroupDN(permGroupID))),with(equal(permissionGroupLDAP)));
				}
			});
			final MESSAGES result = permissionGroupManagement.modify(permissionGroupLDAP, mockedContext);
			Assert.assertEquals(result, MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyPermGroupNamingException() throws Exception {
		final String permGroupID = "permissionGroup1";
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).rebind(with(equal(getPermGroupDN(permGroupID))),with(equal(permissionGroupLDAP)));
					will(throwException(new NamingException()));
				}
			});
			final MESSAGES result = permissionGroupManagement.modify(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyPermGroupGeneralException() throws Exception {
		final String permGroupID = "permissionGroup1";
		final PermissionGroupVO vo = new PermissionGroupVO();
		vo.setPredefined(false);
		vo.setPermissionGroupName(permGroupID);
		final PermissionGroup permissionGroupLDAP = new PermissionGroup(vo);

		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).rebind(with(equal(getPermGroupDN(permGroupID))),with(equal(permissionGroupLDAP)));
					will(throwException(new NullPointerException()));
				}
			});
			final MESSAGES result = permissionGroupManagement.modify(permissionGroupLDAP, mockedContext);
			Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		}  catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	private String getPermGroupDN(final String permGroupID) {
		final StringBuffer permGroupDN = new StringBuffer();
		permGroupDN.append(COMMON_NAME);
		permGroupDN.append(OP_EQUALS);
		permGroupDN.append(permGroupID);
		permGroupDN.append(OP_COMMA);
		permGroupDN.append(PERMISSIONGROUP_BASE_DN);
		return permGroupDN.toString();
	}


	class PermissionGroupManagementStub extends PermissionGroupManagement {

		private final NamingEnumeration<SearchResult> searchResults;
		private final boolean isPermissionValid;

		protected PermissionGroupManagementStub(final NamingEnumeration<SearchResult> searchResults, final boolean isPermissionValid) {
			this.searchResults = searchResults;
			this.isPermissionValid = isPermissionValid;
		}

		@Override
		protected NamingEnumeration<SearchResult> searchLDAP(final DirContext conn, final String filter, final SearchControls searchControl,
				final String baseDn) throws NamingException {
			return searchResults;
		}

		@Override
		protected boolean isAllPermissionsValid(final Set<String> permissions, final DirContext conn) throws LDAPException {
			return isPermissionValid;
		}

		@Override
		protected void removeUniqueMemberReferenceInRole(final String roleDN,
				final String permGroupDN, final DirContext conn) throws NamingException {
			return;
		}
	}

}
