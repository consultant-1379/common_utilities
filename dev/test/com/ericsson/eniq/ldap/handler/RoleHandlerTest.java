package com.ericsson.eniq.ldap.handler;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.common.testutilities.BaseUnitTestX;
import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.Role;
import com.ericsson.eniq.ldap.management.ILDAPManagement;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.management.RoleManagement;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.RoleVO;

public class RoleHandlerTest extends BaseUnitTestX {

	private IHandler handler;
	private DirContext mockedContext;
	private static final String ONLY_LDAP_EXCEPTION_MUST_BE_THROWN = "Only LDAP Exception must be thrown";

	@Before
	public void setUp() throws Exception {
		recreateMockeryContext();
		handler = new RoleHandler();
		mockedContext = context.mock(DirContext.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

	}

	@Test(expected = LDAPException.class)
	public void testCreateException() throws LDAPException, NamingException {

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, false, MESSAGES.ERR_UNEXPECTED_EXCEPTION, null);
		final RoleVO ldapVO = new RoleVO();
		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
				one(mockedContext).close();
				will(throwException(new Exception()));
			}

		});

		final String result = handlerMock.create(loginvo, ldapVO);
	}

	@Test(expected = LDAPException.class)
	public void testCreateLDAPException() throws LDAPException, NamingException {

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, false, MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final RoleVO ldapVO = new RoleVO();

		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
				one(mockedContext).close();
				will(throwException(new LDAPException(MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage())));
			}

		});

		final String result = handlerMock.create(loginvo, ldapVO);
	}

	@Test
	public void testCreateSuccess() throws LDAPException, NamingException {

		final String expected = MESSAGES.MSG_SUCCESS.getMessage();
		String actual = "";

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, true, MESSAGES.MSG_SUCCESS, null);
		final RoleVO ldapVO = new RoleVO();

		final LoginVO loginvo = new LoginVO();

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
					allowing(mockedContext).close();
				}

			});
			actual = handlerMock.create(loginvo, ldapVO);
		} catch (final LDAPException e) {
			e.printStackTrace();
		}

		actual = handlerMock.create(loginvo, ldapVO);
		assertEquals(expected, actual);
	}

	@Test
	public void testFindByIdSuccess() throws LDAPException, NamingException {
		final String description = "Description: ";
		final String roleNameDesciption = "Role Name: ";
		final RoleVO roleVo = new RoleVO(); //
		final String roleName = "sysadmin";
		final String roleDescription = "This permission group contains all UI network permissions";

		roleVo.setRoleName(roleName);
		roleVo.setDescription(roleDescription);

		final LoginVO loginVo = new LoginVO();

		final StringBuilder expected = new StringBuilder();

		expected.append(roleNameDesciption);
		expected.append(roleVo.getRoleName());
		expected.append(description);
		expected.append(roleVo.getDescription());

		final Role role = new Role(roleVo);

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, true, MESSAGES.MSG_SUCCESS, role);

		final StringBuilder actual = new StringBuilder();
		RoleVO actualVo = null;

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
					allowing(mockedContext).close();
				}
			});
			actualVo = (RoleVO) handlerMock.findById(loginVo, roleVo);
		} catch (final LDAPException e) {
			e.printStackTrace();
		}
		actualVo = (RoleVO) handlerMock.findById(loginVo, roleVo);

		actual.append(roleNameDesciption);
		actual.append(actualVo.getRoleName());
		actual.append(description);
		actual.append(actualVo.getDescription());

		assertEquals(expected.toString(), actual.toString());
	}

	@Test(expected = LDAPException.class)
	public void testFindByIdException() throws LDAPException, NamingException {

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, false, MESSAGES.ERR_UNEXPECTED_EXCEPTION, null);
		final RoleVO ldapVO = new RoleVO();
		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				one(mockedContext).close();
				will(throwException(new Exception()));
			}

		});

		final RoleVO result = (RoleVO) handlerMock.findById(loginvo, ldapVO);
	}

	@Test(expected = LDAPException.class)
	public void testFindByIdLDAPException() throws LDAPException, NamingException {

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, false, MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final RoleVO ldapVO = new RoleVO();

		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				one(mockedContext).close();
				will(throwException(new LDAPException(MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage())));
			}

		});

		final RoleVO result = (RoleVO) handlerMock.findById(loginvo, ldapVO);
	}

	@Test
	public void testFindByIdPermissionsDoNotExistError() {

		final String expected = MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage();
		String actual = "";

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, false, MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final RoleVO ldapVO = new RoleVO();

		final LoginVO loginvo = new LoginVO();
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).close();
				}
			});

			final RoleVO test = (RoleVO) handlerMock.findById(loginvo, ldapVO);

		} catch (final LDAPException e) {
			actual = e.getMessage();
			assertEquals(expected, actual);
		} catch (final NamingException e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	/*
	 * @Test public void testFindAllSuccess() throws LDAPException,
	 * NamingException { final String description = "Description: "; final
	 * String roleNameDesciption = "Role Name: "; final RoleVO roleVo = new
	 * RoleVO(); // final String roleName = "sysadmin"; final String
	 * roleDescription =
	 * "This permission group contains all UI network permissions";
	 * 
	 * roleVo.setRoleName(roleName); roleVo.setDescription(roleDescription);
	 * 
	 * final LoginVO loginVo = new LoginVO();
	 * 
	 * final StringBuilder expected = new StringBuilder();
	 * 
	 * expected.append(roleNameDesciption);
	 * expected.append(roleVo.getRoleName()); expected.append(description);
	 * expected.append(roleVo.getDescription());
	 * 
	 * final Role role = new Role(roleVo);
	 * 
	 * final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext,
	 * true, MESSAGES.MSG_SUCCESS, role);
	 * 
	 * final StringBuilder actual = new StringBuilder(); List<IValueObject>
	 * actualVo = null;
	 * 
	 * try { context.checking(new Expectations() { {
	 * allowing(mockedContext).search(with(any(String.class)),
	 * with(any(String.class)), with(any(SearchControls.class)));
	 * allowing(mockedContext).close(); } }); actualVo =
	 * handlerMock.findAll(loginVo); } catch (final LDAPException e) {
	 * e.printStackTrace(); } actualVo = handlerMock.findAll(loginVo);
	 * 
	 * actual.append(roleNameDesciption); actual.append(actualVo.getRoleName());
	 * actual.append(description); actual.append(actualVo.getDescription());
	 * 
	 * assertEquals(expected.toString(), actual.toString()); }
	 */
	@Test(expected = LDAPException.class)
	public void testFindAllException() throws LDAPException, NamingException {

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, false, MESSAGES.ERR_UNEXPECTED_EXCEPTION, null);
		final RoleVO ldapVO = new RoleVO();
		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
				one(mockedContext).close();
				will(throwException(new Exception()));
			}

		});

		final List<IValueObject> result = handlerMock.findAll(loginvo);
	}

	@Test(expected = LDAPException.class)
	public void testFindAllLDAPException() throws LDAPException, NamingException {

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, false, MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final RoleVO ldapVO = new RoleVO();

		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
				one(mockedContext).close();
				will(throwException(new LDAPException(MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage())));
			}

		});

		final List<IValueObject> result = handlerMock.findAll(loginvo);
	}

	@Test
	public void testFindAllPermissionsDoNotExistError() {

		final String expected = MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage();
		String actual = "";

		final RoleHandlerMock handlerMock = new RoleHandlerMock(mockedContext, false, MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final RoleVO ldapVO = new RoleVO();

		final LoginVO loginvo = new LoginVO();
		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
					one(mockedContext).close();
				}
			});

			final List<IValueObject> test = handlerMock.findAll(loginvo);

		} catch (final LDAPException e) {
			actual = e.getMessage();
			assertEquals(expected, actual);
		} catch (final NamingException e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	class RoleHandlerMock extends RoleHandler {
		private final DirContext dirCxt;
		private final boolean isRoleValid;
		private final MESSAGES createMessage;
		private final ILDAPObject role;

		public RoleHandlerMock(final DirContext dirCxt, final boolean isRoleValid, final MESSAGES createMessage, final ILDAPObject role) {
			this.dirCxt = dirCxt;
			this.isRoleValid = isRoleValid;
			this.createMessage = createMessage;
			this.role = role;
		}

		@Override
		protected DirContext getLDAPConnection(final LoginVO login) throws LDAPException {
			return dirCxt;
		}

		@Override
		protected ILDAPManagement getRoleManagement() {
			final ILDAPManagement roleManagement = new RoleManagementMock(isRoleValid, createMessage, role);
			return roleManagement;
		}

		@Override
		protected MESSAGES getAllowedMessage(final LoginVO login, final RoleVO roleVO, final DirContext ctxt) throws LDAPException {
			return createMessage;
		}
	}

	class RoleManagementMock extends RoleManagement {

		private final boolean isRoleValid;
		private final MESSAGES createMessage;
		private final ILDAPObject role;

		protected RoleManagementMock(final boolean isRoleValid, final MESSAGES createMessage, final ILDAPObject role) {
			this.isRoleValid = isRoleValid;
			this.createMessage = createMessage;
			this.role = role;
		}

		/*
		 * @Override protected boolean isAllRolesValid(final Set<String>
		 * permissions, final DirContext conn) throws LDAPException { return
		 * isRoleValid; }
		 */

		@Override
		public ILDAPObject findById(final String id, final DirContext conn) throws LDAPException {
			return role;
		}

		@Override
		public MESSAGES create(final ILDAPObject objLdap, final DirContext conn) throws LDAPException {
			return createMessage;
		}
	}
}
