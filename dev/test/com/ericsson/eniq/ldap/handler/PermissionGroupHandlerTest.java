package com.ericsson.eniq.ldap.handler;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.common.testutilities.BaseUnitTestX;
import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.PermissionGroup;
import com.ericsson.eniq.ldap.management.ILDAPManagement;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.management.PermissionGroupManagement;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.PermissionGroupVO;

public class PermissionGroupHandlerTest extends BaseUnitTestX {

	private IHandler handler;
	private DirContext mockedContext;
	private static final String ONLY_LDAP_EXCEPTION_MUST_BE_THROWN = "Only LDAP Exception must be thrown";

	@Before
	public void setup() {
		recreateMockeryContext();
		handler = new PermissionGroupHandler();
		mockedContext = context.mock(DirContext.class);
	}

	@Test(expected = LDAPException.class)
	public void testCreateException() throws LDAPException, NamingException {

		final PermissionGroupHandlerMock handlerMock = new PermissionGroupHandlerMock(mockedContext, false, MESSAGES.ERR_UNEXPECTED_EXCEPTION, null);
		final PermissionGroupVO ldapVO = new PermissionGroupVO();
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

		final PermissionGroupHandlerMock handlerMock = new PermissionGroupHandlerMock(mockedContext, false,
				MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final PermissionGroupVO ldapVO = new PermissionGroupVO();

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

	/*
	 * @Test public void testCreatePermissionsDoNotExistError() {
	 * 
	 * final String expected =
	 * MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage(); String
	 * actual = null;
	 * 
	 * final PermissionGroupHandlerMock handlerMock = new
	 * PermissionGroupHandlerMock(mockedContext, false,
	 * MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null); final
	 * PermissionGroupVO ldapVO = new PermissionGroupVO();
	 * 
	 * final LoginVO loginvo = new LoginVO(); try { context.checking(new
	 * Expectations() { {
	 * allowing(mockedContext).search(with(any(String.class)),
	 * with(any(String.class)), with(any(SearchControls.class)));
	 * one(mockedContext).close(); } });
	 * 
	 * actual = handlerMock.create(loginvo, ldapVO);
	 * System.out.println("This is the result: " + actual); } catch (final
	 * LDAPException e) { actual = e.getMessage(); assertEquals(expected,
	 * actual); } catch (final NamingException e) {
	 * System.out.println("This is the result: " + actual);
	 * Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN); } }
	 */
	@Test
	public void testCreateSuccess() throws LDAPException, NamingException {

		final String expected = MESSAGES.MSG_SUCCESS.getMessage();
		String actual = "";

		final PermissionGroupHandlerMock handlerMock = new PermissionGroupHandlerMock(mockedContext, true, MESSAGES.MSG_SUCCESS, null);
		final PermissionGroupVO ldapVO = new PermissionGroupVO();

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
		final String groupName = "Group Name: ";
		final PermissionGroupVO permGrpVo = new PermissionGroupVO();
		final String permissionGroupName = "networkgroup";
		final String groupDescription = "This permission group contains all UI network permissions";

		permGrpVo.setPermissionGroupName(permissionGroupName);
		permGrpVo.setDescription(groupDescription);

		final LoginVO loginVo = new LoginVO();

		final StringBuilder expected = new StringBuilder();

		expected.append(groupName);
		expected.append(permGrpVo.getPermissionGroupName());
		expected.append(description);
		expected.append(permGrpVo.getDescription());

		final PermissionGroup permGroup = new PermissionGroup(permGrpVo);

		final PermissionGroupHandlerMock handlerMock = new PermissionGroupHandlerMock(mockedContext, true, MESSAGES.MSG_SUCCESS, permGroup);

		final StringBuilder actual = new StringBuilder();
		PermissionGroupVO actualVo = null;

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).close();
				}

			});
			actualVo = (PermissionGroupVO) handlerMock.findById(loginVo, permGrpVo);
		} catch (final LDAPException e) {
			e.printStackTrace();
		}
		actualVo = (PermissionGroupVO) handlerMock.findById(loginVo, permGrpVo);

		actual.append(groupName);
		actual.append(actualVo.getPermissionGroupName());
		actual.append(description);
		actual.append(actualVo.getDescription());

		assertEquals(expected.toString(), actual.toString());
	}

	@Test(expected = LDAPException.class)
	public void testFindByIdException() throws LDAPException, NamingException {

		final PermissionGroupHandlerMock handlerMock = new PermissionGroupHandlerMock(mockedContext, false, MESSAGES.ERR_UNEXPECTED_EXCEPTION, null);
		final PermissionGroupVO ldapVO = new PermissionGroupVO();
		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				one(mockedContext).close();
				will(throwException(new Exception()));
			}

		});

		final PermissionGroupVO result = (PermissionGroupVO) handlerMock.findById(loginvo, ldapVO);
	}

	@Test(expected = LDAPException.class)
	public void testFindByIdLDAPException() throws LDAPException, NamingException {

		final PermissionGroupHandlerMock handlerMock = new PermissionGroupHandlerMock(mockedContext, false,
				MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final PermissionGroupVO ldapVO = new PermissionGroupVO();

		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				one(mockedContext).close();
				will(throwException(new LDAPException(MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage())));
			}

		});

		final PermissionGroupVO result = (PermissionGroupVO) handlerMock.findById(loginvo, ldapVO);
	}

	@Test
	public void testFindByIdPermissionsDoNotExistError() {

		final String expected = MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage();
		String actual = "";

		final PermissionGroupHandlerMock handlerMock = new PermissionGroupHandlerMock(mockedContext, false,
				MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final PermissionGroupVO ldapVO = new PermissionGroupVO();

		final LoginVO loginvo = new LoginVO();
		try {
			context.checking(new Expectations() {
				{
					one(mockedContext).close();
				}
			});

			final PermissionGroupVO test = (PermissionGroupVO) handlerMock.findById(loginvo, ldapVO);

		} catch (final LDAPException e) {
			actual = e.getMessage();
			assertEquals(expected, actual);
		} catch (final NamingException e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}

	}

	class PermissionGroupHandlerMock extends PermissionGroupHandler {
		private final DirContext dirCxt;
		private final boolean isPermissionValid;
		private final MESSAGES createMessage;
		private final ILDAPObject permGroup;

		public PermissionGroupHandlerMock(final DirContext dirCxt, final boolean isPermissionValid, final MESSAGES createMessage,
				final ILDAPObject permGroup) {
			this.dirCxt = dirCxt;
			this.isPermissionValid = isPermissionValid;
			this.createMessage = createMessage;
			this.permGroup = permGroup;
		}

		@Override
		protected DirContext getLDAPConnection(final LoginVO login) throws LDAPException {
			return dirCxt;
		}

		@Override
		protected ILDAPManagement getPermissionGroupManagement() {
			final ILDAPManagement permGroupManagement = new PermissionGroupManagementMock(isPermissionValid, createMessage, permGroup);
			return permGroupManagement;
		}

		@Override
		protected MESSAGES getAllowedMessage(final LoginVO login, final PermissionGroupVO permGroupVO, final DirContext ctxt) throws LDAPException {
			return createMessage;
		}
	}

	class PermissionGroupManagementMock extends PermissionGroupManagement {

		private final boolean isPermissionValid;
		private final MESSAGES createMessage;
		private final ILDAPObject permGroup;

		protected PermissionGroupManagementMock(final boolean isPermissionValid, final MESSAGES createMessage, final ILDAPObject permGroup) {
			this.isPermissionValid = isPermissionValid;
			this.createMessage = createMessage;
			this.permGroup = permGroup;
		}

		@Override
		protected boolean isAllPermissionsValid(final Set<String> permissions, final DirContext conn) throws LDAPException {
			return isPermissionValid;
		}

		@Override
		public ILDAPObject findById(final String id, final DirContext conn) throws LDAPException {
			return permGroup;
		}

		@Override
		public MESSAGES create(final ILDAPObject objLdap, final DirContext conn) throws LDAPException {
			return createMessage;
		}

	}

}
