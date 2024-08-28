package com.ericsson.eniq.ldap.handler;

import static org.junit.Assert.assertEquals;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.eniq.common.testutilities.BaseUnitTestX;
import com.ericsson.eniq.ldap.entity.ILDAPObject;
import com.ericsson.eniq.ldap.entity.User;
import com.ericsson.eniq.ldap.management.ILDAPManagement;
import com.ericsson.eniq.ldap.management.LDAPException;
import com.ericsson.eniq.ldap.management.UserManagement;
import com.ericsson.eniq.ldap.management.UserProfileManagement;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.vo.LoginVO;
import com.ericsson.eniq.ldap.vo.UserVO;

public class UserHandlerTest extends BaseUnitTestX {

	private IHandler handler;
	private DirContext mockedContext;
	private UserProfileManagement mockedUserProfileMgt;

	private static final String ONLY_LDAP_EXCEPTION_MUST_BE_THROWN = "Only LDAP Exception must be thrown";

	@Before
	public void setUp() throws Exception {
		recreateMockeryContext();
		handler = new UserHandler();
		mockedContext = context.mock(DirContext.class);
		mockedUserProfileMgt = context.mock(UserProfileManagement.class);
	}

	@Test(expected = LDAPException.class)
	public void testCreateException() throws LDAPException, NamingException {

		final UserHandlerMock handlerMock = new UserHandlerMock(mockedContext, false, MESSAGES.ERR_UNEXPECTED_EXCEPTION, null);
		final UserVO ldapVO = new UserVO();
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

		final UserHandlerMock handlerMock = new UserHandlerMock(mockedContext, false, MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final UserVO ldapVO = new UserVO();

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
	 * @Test public void testCreateSuccess() throws LDAPException,
	 * NamingException {
	 * 
	 * final MESSAGES message = MESSAGES.MSG_SUCCESS; final String expected =
	 * message.getMessage(); String actual = "";
	 * 
	 * final UserHandlerMock handlerMock = new UserHandlerMock(mockedContext,
	 * true, message, null); final UserVO ldapVO = new UserVO();
	 * 
	 * final String userFName = "First Name"; final String userLName =
	 * "Last Name"; final String userPassword = "Test12345"; final String userID
	 * = "testid"; final boolean userIsPredefined = true;
	 * 
	 * final UserVO userVo = new UserVO(); //
	 * 
	 * userVo.setFname(userFName); userVo.setLname(userLName);
	 * userVo.setPassword(userPassword); userVo.setUserId(userID);
	 * userVo.setPredefined(userIsPredefined);
	 * 
	 * final Set<String> roles = new HashSet<String>();
	 * roles.add("existingrole"); userVo.setRoles(roles);
	 * 
	 * final LoginVO loginvo = new LoginVO();
	 * 
	 * try { context.checking(new Expectations() { {
	 * allowing(mockedContext).search(with(any(String.class)),
	 * with(any(String.class)), with(any(SearchControls.class)));
	 * allowing(mockedContext).close();
	 * allowing(mockedUserProfileMgt).create(with(any(UserProfile.class)),
	 * with(any(DirContext.class))); will(returnValue(message)); } }); actual =
	 * handlerMock.create(loginvo, ldapVO); } catch (final LDAPException e) {
	 * e.printStackTrace(); }
	 * 
	 * actual = handlerMock.create(loginvo, ldapVO); assertEquals(expected,
	 * actual); }
	 */
	@Test
	public void testFindByIdSuccess() throws LDAPException, NamingException {
		final String userFNameDesc = "First Name: ";
		final String userLNameDesc = "Last Name: ";
		final String userPasswordDesc = "Password: ";
		final String userIDDesc = "User ID: ";
		final String userIsPredefinedDesc = "Predefined: ";

		final String userFName = "First Name";
		final String userLName = "Last Name";
		final String userPassword = "Test12345";
		final String userID = "testid";
		final boolean userIsPredefined = true;

		final UserVO userVo = new UserVO(); //

		userVo.setFname(userFName);
		userVo.setLname(userLName);
		userVo.setPassword(userPassword);
		userVo.setUserId(userID);
		userVo.setPredefined(userIsPredefined);

		final LoginVO loginVo = new LoginVO();

		final StringBuilder expected = new StringBuilder();

		expected.append(userFNameDesc);
		expected.append(userVo.getFname());
		expected.append(userLNameDesc);
		expected.append(userVo.getLname());
		expected.append(userPasswordDesc);
		expected.append(userVo.getPassword());
		expected.append(userIDDesc);
		expected.append(userVo.getUserId());
		expected.append(userIsPredefinedDesc);
		expected.append(userVo.isPredefined());

		final User user = new User(userVo);

		final UserHandlerMock handlerMock = new UserHandlerMock(mockedContext, true, MESSAGES.MSG_SUCCESS, user);

		final StringBuilder actual = new StringBuilder();
		UserVO actualVo = null;

		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
					allowing(mockedContext).close();
				}
			});
			actualVo = (UserVO) handlerMock.findById(loginVo, userVo);
			actual.append(userFNameDesc);
			actual.append(userVo.getFname());
			actual.append(userLNameDesc);
			actual.append(userVo.getLname());
			actual.append(userPasswordDesc);
			actual.append(userVo.getPassword());
			actual.append(userIDDesc);
			actual.append(userVo.getUserId());
			actual.append(userIsPredefinedDesc);
			actual.append(userVo.isPredefined());

			assertEquals(expected.toString(), actual.toString());
		} catch (final LDAPException e) {
			e.printStackTrace();
		}
		// actualVo = (UserVO) handlerMock.findById(loginVo, userVo);

	}

	@Test(expected = LDAPException.class)
	public void testFindByIdException() throws LDAPException, NamingException {

		final UserHandlerMock handlerMock = new UserHandlerMock(mockedContext, false, MESSAGES.ERR_UNEXPECTED_EXCEPTION, null);
		final UserVO ldapVO = new UserVO();
		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
				one(mockedContext).close();
				will(throwException(new Exception()));
			}

		});

		final UserVO result = (UserVO) handlerMock.findById(loginvo, ldapVO);
	}

	@Test(expected = LDAPException.class)
	public void testFindByIdLDAPException() throws LDAPException, NamingException {

		final UserHandlerMock handlerMock = new UserHandlerMock(mockedContext, false, MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, null);
		final UserVO ldapVO = new UserVO();

		final LoginVO loginvo = new LoginVO();
		context.checking(new Expectations() {
			{
				allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
				one(mockedContext).close();
				will(throwException(new LDAPException(MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage())));
			}

		});

		final UserVO result = (UserVO) handlerMock.findById(loginvo, ldapVO);
	}

	@Test
	public void testFindByIdPermissionsDoNotExistError() {

		final String expected = MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST.getMessage();
		String actual = "";
		final UserVO ldapVO = new UserVO();
		ldapVO.setUserId("admin");
		final LoginVO loginvo = new LoginVO();
		
		final UserHandlerMock handlerMock = new UserHandlerMock(mockedContext, false, MESSAGES.ERR_SELECTED_PERMISSIONS_DO_NOT_EXIST, new User(ldapVO));
		
		try {
			context.checking(new Expectations() {
				{
					allowing(mockedContext).search(with(any(String.class)), with(any(String.class)), with(any(SearchControls.class)));
					one(mockedContext).close();
				}
			});

			final UserVO test = (UserVO) handlerMock.findById(loginvo, ldapVO);

		} catch (final LDAPException e) {
			actual = e.getMessage();
			assertEquals(expected, actual);
		} catch (final NamingException e) {
			Assert.fail(ONLY_LDAP_EXCEPTION_MUST_BE_THROWN);
		}
	}

	class UserHandlerMock extends UserHandler {
		private final DirContext dirCxt;
		private final boolean isUserValid;
		private final MESSAGES createMessage;
		private final ILDAPObject user;

		public UserHandlerMock(final DirContext dirCxt, final boolean isUserValid, final MESSAGES createMessage, final ILDAPObject user) {
			this.dirCxt = dirCxt;
			this.isUserValid = isUserValid;
			this.createMessage = createMessage;
			this.user = user;
		}

		@Override
		protected DirContext getLDAPConnection(final LoginVO login) throws LDAPException {
			return dirCxt;
		}

		@Override
		protected ILDAPManagement getUserManagementInstance() {
			final ILDAPManagement userManagement = new UserManagementMock(isUserValid, createMessage, user);
			return userManagement;
		}		
	}

	class UserManagementMock extends UserManagement {

		private final boolean isUserValid;
		private final MESSAGES createMessage;
		private final ILDAPObject user;

		protected UserManagementMock(final boolean isUserValid, final MESSAGES createMessage, final ILDAPObject user) {
			this.isUserValid = isUserValid;
			this.createMessage = createMessage;
			this.user = user;
		}

		/*
		 * @Override protected boolean isAllUsersValid(final Set<String>
		 * permissions, final DirContext conn) throws LDAPException { return
		 * isUserValid; }
		 */

		@Override
		public ILDAPObject findById(final String id, final DirContext conn) throws LDAPException {
			return user;
		}

		@Override
		public MESSAGES create(final ILDAPObject objLdap, final DirContext conn) throws LDAPException {
			return createMessage;
		}

	}

}
