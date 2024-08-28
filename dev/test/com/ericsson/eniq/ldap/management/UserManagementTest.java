package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.OBJECT_CLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PREDEFINED_SYSADMIN_USER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USER;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERS_BASE_DN;
import static com.ericsson.eniq.ldap.util.LDAPConstants.LEFT_BRACE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_AND;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_COMMA;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_EQUALS;
import static com.ericsson.eniq.ldap.util.LDAPConstants.PASSWORD_IN_PASSWORD_HISTORY_EXCEPTION_MESSAGE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.PASSWORD_PLACEHOLDER;
import static com.ericsson.eniq.ldap.util.LDAPConstants.RIGHT_BRACE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
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
import com.ericsson.eniq.ldap.entity.User;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.vo.UserVO;

/**
 * @author eramano
 *
 */
public class UserManagementTest extends BaseUnitTestX {
	private ILDAPManagement userManagement;
	private DirContext mockedContext;
	private NamingEnumeration<SearchResult> mockedNamingEnumeration;
	private SearchResult mockedSearchResult;
	private Attributes mockedAttributes;
	private final String USERNAME = "newuser";
	private String userDN;
	private String userFilterShort;
	private String userFilterLong;
	  
	  
	@Before
	public void setup() {
		recreateMockeryContext();
	    userManagement = new UserManagement();
	    userFilterShort = getFilterShort();
	    userFilterLong = getFilterLong();
	    userDN = getUserDN(USERNAME);
	    mockedContext = context.mock(DirContext.class);
	    mockedNamingEnumeration = context.mock(NamingEnumeration.class);
		mockedSearchResult = context.mock(SearchResult.class);
		mockedAttributes = context.mock(Attributes.class);
	}
		
	@Test
	public void testCreateUserSuccess(){
		final User user = new User(getUIUserVO());
		try {
			 context.checking(new Expectations() {
				 {
					 // bind user
					 one(mockedContext).bind(with(equal(userDN)), with(equal(user)));
				 }
			  });
			  MESSAGES result = userManagement.create(user, mockedContext);
			  Assert.assertEquals(result, MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateDuplicateUser(){
		final User duplicateUser = new User(getUIUserVO());
		try {
			 context.checking(new Expectations() {
				 {
					 // bind user
					 one(mockedContext).bind(with(equal(userDN)), with(equal(duplicateUser)));
					 will(throwException(new NameAlreadyBoundException()));
				 }
			  });
			  MESSAGES result = userManagement.create(duplicateUser, mockedContext);
			  Assert.fail("Excepted LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_USER_ALREADY_EXISTS.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateUserThrowingNamingExceptionWhileLDAPBind(){
		final User user = new User(getUIUserVO());
		try {
			 context.checking(new Expectations() {
				 {
					 // bind user
					 one(mockedContext).bind(with(equal(userDN)), with(equal(user)));
					 will(throwException(new NamingException()));
				 }
			  });
			  MESSAGES result = userManagement.create(user, mockedContext);
			  Assert.fail("Excepted LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateUserThrowingGeneralExceptionWhileLDAPBind(){
		final User user = new User(getUIUserVO());
		try {
			 context.checking(new Expectations() {
				 {
					 // bind user
					 one(mockedContext).bind(with(equal(userDN)), with(equal(user)));
					 will(throwException(new Exception()));
				 }
			  });
			  MESSAGES result = userManagement.create(user, mockedContext);
			  Assert.fail("Excepted LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindAll(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(USERS_BASE_DN)), with(equal(userFilterShort)),
	    					with(any(SearchControls.class)));
	    			will(returnValue(mockedNamingEnumeration));
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(true));
	    			one(mockedNamingEnumeration).next();
	    			will(returnValue(mockedSearchResult));
	    			one(mockedSearchResult).getAttributes();
	    			will(returnValue(mockedAttributes));
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(false));
	    		}
	    	});
            List<ILDAPObject> result = userManagement.findAll(mockedContext);
            Assert.assertTrue("Only one user must be present", result.size() == 1);
            Assert.assertEquals(mockedAttributes, result.get(0).getAttributes(""));
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindAllWhileNoUsersConfigured(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(USERS_BASE_DN)), with(equal(userFilterShort)),
	    					with(any(SearchControls.class)));
	    			will(returnValue(mockedNamingEnumeration));
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(false));	    		
	    		}
	    	});
            List<ILDAPObject> result = userManagement.findAll(mockedContext);
            Assert.assertTrue("No user retrieved", result.isEmpty());
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindAllThrowingNamingException(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(USERS_BASE_DN)), with(equal(userFilterShort)),
	    					with(any(SearchControls.class)));
	    			will(throwException(new NamingException()));
	    		}
	    	});
            List<ILDAPObject> result = userManagement.findAll(mockedContext);
            Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindAllThrowingGeneralException(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(USERS_BASE_DN)), with(equal(userFilterShort)),
	    					with(any(SearchControls.class)));
	    			will(throwException(new Exception()));
	    		}
	    	});
            List<ILDAPObject> result = userManagement.findAll(mockedContext);
            Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindById(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(USERS_BASE_DN)), with(equal(userFilterLong)),
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
            ILDAPObject result = userManagement.findById(USERNAME,mockedContext);
            Assert.assertTrue("Must return a user", result != null );
            Assert.assertEquals(mockedAttributes, result.getAttributes(""));
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindByIdWhileNoUserExists(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(USERS_BASE_DN)), with(equal(userFilterLong)),
	    		    with(any(SearchControls.class)));
	    			will(returnValue(mockedNamingEnumeration));
	    		    one(mockedNamingEnumeration).hasMore();
	    		    will(returnValue(false));
	    		}
	    	});
            ILDAPObject result = userManagement.findById(USERNAME,mockedContext);
            Assert.assertTrue("Must return null", result == null );
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindByIdThrowingNamingException(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(USERS_BASE_DN)), with(equal(userFilterLong)),
	    		    with(any(SearchControls.class)));
	    			will(throwException(new NamingException()));
	    		}
	    	});
            ILDAPObject result = userManagement.findById(USERNAME,mockedContext);
            Assert.fail("Expected LDAPExeption not thrown" );
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindByIdThrowingGeneralException(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(USERS_BASE_DN)), with(equal(userFilterLong)),
	    		    with(any(SearchControls.class)));
	    			will(throwException(new Exception()));
	    		}
	    	});
            ILDAPObject result = userManagement.findById(USERNAME,mockedContext);
            Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testDeleteUserSuccess(){
		final User user = new User(getUIUserVO());
		try {
			 context.checking(new Expectations() {
				 {
					 // unbind user
					 one(mockedContext).unbind(with(equal(userDN)));
				 }
			  });
			  MESSAGES result = userManagement.delete(user, mockedContext);
			  Assert.assertEquals(result, MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testDeleteUserThrowingNamingExceptionWhileLDAPBind(){
		final User user = new User(getUIUserVO());
		try {
			 context.checking(new Expectations() {
				 {
					 // unbind user
					 one(mockedContext).unbind(with(equal(userDN)));
					 will(throwException(new NamingException()));
				 }
			  });
			  MESSAGES result = userManagement.delete(user, mockedContext);
			  Assert.fail("Excepted LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testDeleteUserThrowingGeneralExceptionWhileLDAPBind(){
		final User user = new User(getUIUserVO());
		try {
			 context.checking(new Expectations() {
				 {
					 // unbind user
					 one(mockedContext).unbind(with(equal(userDN)));
					 will(throwException(new Exception()));
				 }
			  });
			  MESSAGES result = userManagement.delete(user, mockedContext);
			  Assert.fail("Excepted LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(), MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyUIUserSuccess(){
		final User user = new User(getUIUserVO());
		
		try {
			 context.checking(new Expectations() {
				 {
					 one(mockedContext).modifyAttributes(with(equal(userDN)),
							 with(equal(DirContext.REPLACE_ATTRIBUTE)),with(any(Attributes.class)));
				 }
			  });
			 UserManagementStub userManagementStub = new UserManagementStub(user);
			 MESSAGES result = userManagementStub.modify(user, mockedContext);
			 Assert.assertEquals(result,MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyAdminUserSuccess(){
		final User user = new User(getAdminUserVO());
		final String adminUserDN = getUserDN(PREDEFINED_SYSADMIN_USER);
		try {
			 context.checking(new Expectations() {
				 {
					 one(mockedContext).modifyAttributes(with(equal(adminUserDN)),
							 with(equal(DirContext.REPLACE_ATTRIBUTE)),with(any(Attributes.class)));
				 }
			  });
			 UserManagementStub userManagementStub = new UserManagementStub(user);
			 MESSAGES result = userManagementStub.modify(user, mockedContext);
			 Assert.assertEquals(result,MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyUserPasswordValidation(){
		final User user = new User(getAdminUserVO());
		final String adminUserDN = getUserDN(PREDEFINED_SYSADMIN_USER);
		try {
			 context.checking(new Expectations() {
				 {
					 one(mockedContext).modifyAttributes(with(equal(adminUserDN)),
							 with(equal(DirContext.REPLACE_ATTRIBUTE)),with(any(Attributes.class)));
					 will(throwException(new NamingException(PASSWORD_IN_PASSWORD_HISTORY_EXCEPTION_MESSAGE)));
				 }
			  });
			 UserManagementStub userManagementStub = new UserManagementStub(user);
			 MESSAGES result = userManagementStub.modify(user, mockedContext);
			 Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_PASSWORD_IN_PASSWORD_HISTORY.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyUserNamingException(){
		final User user = new User(getAdminUserVO());
		final String adminUserDN = getUserDN(PREDEFINED_SYSADMIN_USER);
		try {
			 context.checking(new Expectations() {
				 {
					 one(mockedContext).modifyAttributes(with(equal(adminUserDN)),
							 with(equal(DirContext.REPLACE_ATTRIBUTE)),with(any(Attributes.class)));
					 will(throwException(new NamingException("naming exception")));
				 }
			  });
			 UserManagementStub userManagementStub = new UserManagementStub(user);
			 MESSAGES result = userManagementStub.modify(user, mockedContext);
			 Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyUserGeneralException(){
		final User user = new User(getAdminUserVO());
		final String adminUserDN = getUserDN(PREDEFINED_SYSADMIN_USER);
		try {
			 context.checking(new Expectations() {
				 {
					 one(mockedContext).modifyAttributes(with(equal(adminUserDN)),
							 with(equal(DirContext.REPLACE_ATTRIBUTE)),with(any(Attributes.class)));
					 will(throwException(new NullPointerException()));
				 }
			  });
			 UserManagementStub userManagementStub = new UserManagementStub(user);
			 MESSAGES result = userManagementStub.modify(user, mockedContext);
			 Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	private UserVO getUIUserVO() {
		UserVO userVO = new UserVO();
		userVO.setUserId(USERNAME);
		userVO.setFname("fname");
		userVO.setLname("lname");
		userVO.setEmail("email@ericsson.com");
		userVO.setOrg("ENIQ");
		userVO.setPassword("secret");
		userVO.setPhone("123456");
		userVO.setPredefined(false);
		final Set<String> roles = new HashSet<String>();
		roles.add("terminalspecialist");
		userVO.setRoles(roles);
		return userVO;
	}

	private UserVO getAdminUserVO() {
		UserVO userVO = new UserVO();
		userVO.setUserId(PREDEFINED_SYSADMIN_USER);
		userVO.setFname("fname");
		userVO.setLname("lname");
		userVO.setEmail("email@ericsson.com");
		userVO.setOrg("ENIQ");
		userVO.setPassword(PASSWORD_PLACEHOLDER);
		userVO.setPhone("123456");
		userVO.setPredefined(true);
		final Set<String> roles = new HashSet<String>();
		roles.add("sysadmin");
		userVO.setRoles(roles);
		return userVO;
	}
	
	private String getFilterLong() {
		final StringBuffer filter = new StringBuffer();
		filter.append(LEFT_BRACE);
		filter.append(OP_AND);
		filter.append(LEFT_BRACE);
		filter.append(OBJECT_CLASS);
		filter.append(OP_EQUALS);
		filter.append(USER); 
		filter.append(RIGHT_BRACE);
		filter.append(LEFT_BRACE);
		filter.append(LEFT_BRACE);
		filter.append(UID);
		filter.append(OP_EQUALS);
		filter.append(USERNAME);
		filter.append(RIGHT_BRACE);
		filter.append(RIGHT_BRACE);
		filter.append(RIGHT_BRACE);
		return filter.toString();
	}

	private String getFilterShort() {
		final StringBuilder filter = new StringBuilder();
		filter.append(LEFT_BRACE);
		filter.append(OBJECT_CLASS);
		filter.append(OP_EQUALS);
		filter.append(USER);
		filter.append(RIGHT_BRACE);
		return filter.toString();
	}
	
	private String getUserDN(final String userName){
		  final StringBuffer dn = new StringBuffer();
		  dn.append(UID);
		  dn.append(OP_EQUALS);
		  dn.append(userName);
		  dn.append(OP_COMMA);
		  dn.append(USERS_BASE_DN);
		  return dn.toString();
	  }
	
	/**
	 * @author eramano
	 * Stubbed user management class for unit testing
	 *
	 */
	class UserManagementStub extends UserManagement {
		
		final ILDAPObject user;
		
		UserManagementStub(User user){
			this.user = user;
		}
		
		@Override
		public ILDAPObject findById(final String id, final DirContext conn) throws LDAPException {
		    return this.user;	
		}
	}

}
