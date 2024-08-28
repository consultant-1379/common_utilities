package com.ericsson.eniq.ldap.management;

import static com.ericsson.eniq.ldap.management.LDAPAttributes.COMMON_NAME;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.OBJECT_CLASS;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.PREDEFINED_SYSADMIN_ROLE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLE;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.ROLES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.UID;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERPROFILES_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USERS_BASE_DN;
import static com.ericsson.eniq.ldap.management.LDAPAttributes.USER_PROFILE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.LEFT_BRACE;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_AND;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_COMMA;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_EQUALS;
import static com.ericsson.eniq.ldap.util.LDAPConstants.OP_OR;
import static com.ericsson.eniq.ldap.util.LDAPConstants.RIGHT_BRACE;

import java.util.HashSet;
import java.util.Hashtable;
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
import com.ericsson.eniq.ldap.entity.UserProfile;
import com.ericsson.eniq.ldap.util.MESSAGES;
import com.ericsson.eniq.ldap.vo.UserProfileVO;



/**
 * 
 * Unit test cases for class UserProfileManagement.
 * @author eramano
 *
 */
public class UserProfileManagementTest extends BaseUnitTestX {
	
	private ILDAPManagement userProfileMgmt;
	
	private DirContext mockedContext; 
	
	private String id; 
	private String userProfile;
	private String userProfileFilterLong;
	private String userProfileFilterShort;

	@Before
	public void setup() {
		recreateMockeryContext();
	    userProfileMgmt = new UserProfileManagement();	    
	    mockedContext = context.mock(DirContext.class);
	    id = "user.id";
	    userProfile = USERPROFILES_BASE_DN;
	    userProfileFilterLong = getUserProfileFilterLong(id);
	    userProfileFilterShort = getUserProfileFilterShort();
	}

	@Test
	public void testFindById(){
	    final NamingEnumeration<SearchResult> mockedNamingEnumeration = context.mock(NamingEnumeration.class);
	    final Attributes mockedAttributes = context.mock(Attributes.class);
	    final SearchResult mockedSearchResult = context.mock(SearchResult.class);
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(userProfile)), with(equal(userProfileFilterLong)),
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
            ILDAPObject	result = userProfileMgmt.findById(id, mockedContext);
            Assert.assertEquals(mockedAttributes, result.getAttributes(""));
		} catch (LDAPException e) {
			Assert.fail("LDAPException not exected");
		} catch (NamingException e) {
			Assert.fail("NamingException not exected");
		}
	}
	
	@Test
	public void testFindByIdSearchExceptionHandling(){
		try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(userProfile)), with(equal(userProfileFilterLong)),
	    					with(any(SearchControls.class)));
	    			will(throwException(new NamingException("naming")));
	    		}
	    	});
            ILDAPObject	result = userProfileMgmt.findById(id, mockedContext);
            Assert.fail("Expected LDAP Exception is not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Only LDAP Exception must be thrown");
		}
	}
	
	@Test
	public void testFindByIdSearchExceptionHandling2(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(userProfile)), with(equal(userProfileFilterLong)),
	    					with(any(SearchControls.class)));
	    			will(throwException(new NullPointerException("null")));
	    		}
	    	});
            ILDAPObject	result = userProfileMgmt.findById(id, mockedContext);
            Assert.fail("Expected Null Pointer Exception is not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Only LDAP Exception must be thrown");
		}
	}
	
	@Test
	public void testFindAll(){
	    final NamingEnumeration<SearchResult> mockedNamingEnumeration = context.mock(NamingEnumeration.class);
	    final SearchResult mockedSearchResult = context.mock(SearchResult.class);
	    final Attributes mockedAttributes = context.mock(Attributes.class);
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(userProfile)), with(equal(userProfileFilterShort)),
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
            List<ILDAPObject> result = userProfileMgmt.findAll(mockedContext);
            Assert.assertTrue("Only one user profile must be present", result.size() == 1);
            Assert.assertEquals(mockedAttributes, result.get(0).getAttributes(""));
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testFindAllSearchExceptionHandling(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(userProfile)), with(equal(userProfileFilterShort)),
	    					with(any(SearchControls.class)));
	    			will(throwException(new NamingException("naming")));
	    		}
	    	});
            List<ILDAPObject> result = userProfileMgmt.findAll(mockedContext);
            Assert.fail("Expected LDAP Exception is not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Only LDAP Exception must be thrown");
		}
	}
	
	@Test
	public void testFindAllSearchExceptionHandling2(){
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).search(with(equal(userProfile)), with(equal(userProfileFilterShort)),
	    					with(any(SearchControls.class)));
	    			will(throwException(new NullPointerException("null")));
	    		}
	    	});
	    	List<ILDAPObject> result = userProfileMgmt.findAll(mockedContext);
            Assert.fail("Expected LDAP Exception is not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Only LDAP Exception must be thrown");
		}
	}
	
	@Test
	public void testCreateSysAdminUserProfileSuccess(){
		//Create LDAP Object for user profile from a User Profile VO
		final UserProfileVO vo = new UserProfileVO();
		vo.setPredefined(false);
		vo.setUserId(id);
		final Set<String> roles = new HashSet<String>();
		roles.add(PREDEFINED_SYSADMIN_ROLE);
		vo.setRoles(roles);
		final UserProfile userProfile=  new UserProfile(vo);
		
		final String userProfileDN = getUserProfileDN(id);
		
	    final NamingEnumeration<SearchResult> mockedNamingEnumeration = context.mock(NamingEnumeration.class);
	    final SearchResult mockedSearchResult = context.mock(SearchResult.class);
	    final Attributes mockedAttributes = context.mock(Attributes.class);
	    final Attribute mockedAttribute = context.mock(Attribute.class);
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			// for permissions search
	    			one(mockedContext).search(with(equal(ROLES_BASE_DN)), with(equal(getRolesFilterLong(roles))),
	    					with(any(SearchControls.class)));
	    			will(returnValue(mockedNamingEnumeration));
	    			// permission exists
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(true));
	    			one(mockedNamingEnumeration).next();
	    			will(returnValue(mockedSearchResult));
	    			one(mockedSearchResult).getAttributes();
	    			will(returnValue(mockedAttributes));
	    		
	    			one(mockedAttributes).get(COMMON_NAME);
	    			will(returnValue(mockedAttribute));
	    			one(mockedAttribute).get();
	    			will(returnValue(PREDEFINED_SYSADMIN_ROLE));
	    			// no more permissions
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(false));
	    			// bind object now
	    			one(mockedContext).bind( with(equal(userProfileDN)),
	    					with(equal(userProfile)));
	    		}
	    	});
	    	
	    	UserProfileManagementMock mockedUserProfileManagement = new UserProfileManagementMock(true);
	    	MESSAGES result = mockedUserProfileManagement.create(userProfile, mockedContext );
	    	Assert.assertEquals(result, MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateUIUserProfileSuccess(){
		//Create LDAP Object for user profile from a User Profile VO
		final UserProfileVO vo = new UserProfileVO();
		vo.setPredefined(false);
		vo.setUserId(id);
		final Set<String> roles = new HashSet<String>();
		roles.add("terminalspecialist");
		vo.setRoles(roles);
		final UserProfile userProfile=  new UserProfile(vo);
		
		final String userProfileDN = getUserProfileDN(id);
		
	    final NamingEnumeration<SearchResult> mockedNamingEnumeration = context.mock(NamingEnumeration.class);
	    final SearchResult mockedSearchResult = context.mock(SearchResult.class);
	    final Attributes mockedAttributes = context.mock(Attributes.class);
	    final Attribute mockedAttribute = context.mock(Attribute.class);
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			// for permissions search
	    			one(mockedContext).search(with(equal(ROLES_BASE_DN)), with(equal(getRolesFilterLong(roles))),
	    					with(any(SearchControls.class)));
	    			will(returnValue(mockedNamingEnumeration));
	    			// permission exists
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(true));
	    			one(mockedNamingEnumeration).next();
	    			will(returnValue(mockedSearchResult));
	    			one(mockedSearchResult).getAttributes();
	    			will(returnValue(mockedAttributes));
	    		
	    			one(mockedAttributes).get(COMMON_NAME);
	    			will(returnValue(mockedAttribute));
	    			one(mockedAttribute).get();
	    			will(returnValue("terminalspecialist"));
	    			// no more permissions
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(false));
	    			// bind object now
	    			one(mockedContext).bind( with(equal(userProfileDN)),
	    					with(equal(userProfile)));
	    		}
	    	});
	    	
	    	UserProfileManagementMock mockedUserProfileManagement = new UserProfileManagementMock(false);
	    	MESSAGES result = mockedUserProfileManagement.create(userProfile, mockedContext );
	    	Assert.assertEquals(result, MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateSysAdminAndUIUserProfileSuccess(){
		//Create LDAP Object for user profile from a User Profile VO
		final UserProfileVO vo = new UserProfileVO();
		vo.setPredefined(false);
		vo.setUserId(id);
		final Set<String> roles = new HashSet<String>();
		roles.add("terminalspecialist");
		roles.add(PREDEFINED_SYSADMIN_ROLE);
		vo.setRoles(roles);
		final UserProfile userProfile=  new UserProfile(vo);
		
		final String userProfileDN = getUserProfileDN(id);
		
	    final NamingEnumeration<SearchResult> mockedNamingEnumeration = context.mock(NamingEnumeration.class);
	    final SearchResult mockedSearchResult = context.mock(SearchResult.class);
	    final Attributes mockedAttributes = context.mock(Attributes.class);
	    final Attribute mockedAttribute = context.mock(Attribute.class);
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			// for permissions search
	    			one(mockedContext).search(with(equal(ROLES_BASE_DN)), with(equal(getRolesFilterLong(roles))),
	    					with(any(SearchControls.class)));
	    			will(returnValue(mockedNamingEnumeration));
	    			// first permission exists
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(true));
	    			one(mockedNamingEnumeration).next();
	    			will(returnValue(mockedSearchResult));
	    			one(mockedSearchResult).getAttributes();
	    			will(returnValue(mockedAttributes));
	    			one(mockedAttributes).get(COMMON_NAME);
	    			will(returnValue(mockedAttribute));
	    			one(mockedAttribute).get();
	    			will(returnValue("terminalspecialist"));
	    			// second permission exists
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(true));
	    			one(mockedNamingEnumeration).next();
	    			will(returnValue(mockedSearchResult));
	    			one(mockedSearchResult).getAttributes();
	    			will(returnValue(mockedAttributes));
	    			one(mockedAttributes).get(COMMON_NAME);
	    			will(returnValue(mockedAttribute));
	    			one(mockedAttribute).get();
	    			will(returnValue(PREDEFINED_SYSADMIN_ROLE));
	    			// no more permissions
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(false));
	    			// bind object now
	    			one(mockedContext).bind( with(equal(userProfileDN)),
	    					with(equal(userProfile)));
	    		}
	    	});
	    	
	    	UserProfileManagementMock mockedUserProfileManagement = new UserProfileManagementMock(true);
	    	MESSAGES result = mockedUserProfileManagement.create(userProfile, mockedContext );
	    	Assert.assertEquals(result, MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateUserProfileWithNonExistingRoles(){
		//Create LDAP Object for user profile from a User Profile VO
		final UserProfileVO vo = new UserProfileVO();
		vo.setPredefined(false);
		vo.setUserId(id);
		final Set<String> roles = new HashSet<String>();
		roles.add("InvalidRole");
		vo.setRoles(roles);
		final UserProfile userProfile=  new UserProfile(vo);
	    final NamingEnumeration<SearchResult> mockedNamingEnumeration = context.mock(NamingEnumeration.class);
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			// for permissions search
	    			one(mockedContext).search(with(equal(ROLES_BASE_DN)), with(equal(getRolesFilterLong(roles))),
	    					with(any(SearchControls.class)));
	    			will(returnValue(mockedNamingEnumeration));
	    			// first permission exists
	    			one(mockedNamingEnumeration).hasMore();
	    			will(returnValue(false));
	    		}
	    	});
	    	MESSAGES result = userProfileMgmt.create(userProfile, mockedContext ); 
	    	Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_SELECTED_ROLES_DO_NOT_EXIST.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateUserProfileNamingExceptionWhileRolesSearch(){
		//Create LDAP Object for user profile from a User Profile VO
		final UserProfileVO vo = new UserProfileVO();
		vo.setPredefined(false);
		vo.setUserId(id);
		final Set<String> roles = new HashSet<String>();
		roles.add(PREDEFINED_SYSADMIN_ROLE);
		vo.setRoles(roles);
		final UserProfile userProfile=  new UserProfile(vo);
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			// for permissions search
	    			one(mockedContext).search(with(equal(ROLES_BASE_DN)), with(equal(getRolesFilterLong(roles))),
	    					with(any(SearchControls.class)));
	    			will(throwException(new NamingException()));
	    		}
	    	});
	    	MESSAGES result = userProfileMgmt.create(userProfile, mockedContext ); 
	    	Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateUserProfileGeneralExceptionWhileRolesSearch(){
		//Create LDAP Object for user profile from a User Profile VO
		final UserProfileVO vo = new UserProfileVO();
		vo.setPredefined(false);
		vo.setUserId(id);
		final Set<String> roles = new HashSet<String>();
		roles.add(PREDEFINED_SYSADMIN_ROLE);
		vo.setRoles(roles);
		final UserProfile userProfile=  new UserProfile(vo);
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			// for permissions search
	    			one(mockedContext).search(with(equal(ROLES_BASE_DN)), with(equal(getRolesFilterLong(roles))),
	    					with(any(SearchControls.class)));
	    			will(throwException(new NullPointerException()));
	    		}
	    	});
	    	MESSAGES result = userProfileMgmt.create(userProfile, mockedContext ); 
	    	Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateUserProfileDuplicateProfile(){
		final UserProfile userProfile=  getSysAdminUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).bind( with(equal(userProfileDN)),
	    					with(equal(userProfile)));
	    			will(throwException(new NameAlreadyBoundException()));
	    		}
	    	});
	    	final UserProfileManagementMock2 mockedUserProfileMgmt =  new UserProfileManagementMock2(true,true);
	    	MESSAGES result = mockedUserProfileMgmt.create(userProfile, mockedContext ); 
	    	Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_USER_PROFILE_ALREADY_EXISTS.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	
	@Test
	public void testCreateUserProfileNamingException(){
		final UserProfile userProfile=  getSysAdminUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).bind( with(equal(userProfileDN)),
	    					with(equal(userProfile)));
	    			will(throwException(new NamingException()));
	    		}
	    	});
	    	final UserProfileManagementMock2 mockedUserProfileMgmt =  new UserProfileManagementMock2(true,true);
	    	MESSAGES result = mockedUserProfileMgmt.create(userProfile, mockedContext ); 
	    	Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testCreateUserProfileGeneralException(){
		final UserProfile userProfile=  getSysAdminUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		
	    try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).bind( with(equal(userProfileDN)),
	    					with(equal(userProfile)));
	    			will(throwException(new NullPointerException()));
	    		}
	    	});
	    	final UserProfileManagementMock2 mockedUserProfileMgmt =  new UserProfileManagementMock2(true,true);
	    	MESSAGES result = mockedUserProfileMgmt.create(userProfile, mockedContext ); 
	    	Assert.fail("Expected LDAPException not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testDeleteAdminUserSuccess(){
		final UserProfile userProfile = getSysAdminUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).unbind( with(equal(userProfileDN)));
	    		}
	    	});
	    	final UserProfileManagementMock mockedUserProfileMgmt =  new UserProfileManagementMock(true);
	    	MESSAGES result = mockedUserProfileMgmt.delete(userProfile, mockedContext ); 
	    	Assert.assertEquals(result,MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAP Exception not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testDeleteUIUserSuccess(){
		final UserProfile userProfile = getUIUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).unbind( with(equal(userProfileDN)));
	    		}
	    	});
	    	final UserProfileManagementMock mockedUserProfileMgmt =  new UserProfileManagementMock(false);
	    	MESSAGES result = mockedUserProfileMgmt.delete(userProfile, mockedContext ); 
	    	Assert.assertEquals(result,MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAP Exception not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testDeleteUserNamingException(){
		final UserProfile userProfile = getSysAdminUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).unbind( with(equal(userProfileDN)));
	    			will(throwException(new NamingException()));
	    		}
	    	});
	    	final UserProfileManagementMock mockedUserProfileMgmt =  new UserProfileManagementMock(true);
	    	MESSAGES result = mockedUserProfileMgmt.delete(userProfile, mockedContext ); 
	    	Assert.fail("Expected LDAP Exception not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testDeleteUserGeneralException(){
		final UserProfile userProfile = getUIUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		try {
	    	context.checking(new Expectations() {
	    		{
	    			one(mockedContext).unbind( with(equal(userProfileDN)));
	    			will(throwException(new NullPointerException()));
	    		}
	    	});
	    	final UserProfileManagementMock mockedUserProfileMgmt =  new UserProfileManagementMock(false);
	    	MESSAGES result = mockedUserProfileMgmt.delete(userProfile, mockedContext );
	    	Assert.fail("Expected LDAP Exception not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyUserProfileInvalidRoles(){
		final UserProfile userProfile = getUIUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		try {
			final UserProfileManagementMock2 mockedUserProfileMgmt =  new UserProfileManagementMock2(false,false);
	    	MESSAGES result = mockedUserProfileMgmt.modify(userProfile, mockedContext);
	    	Assert.fail("Expected LDAP Exception not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_SELECTED_ROLES_DO_NOT_EXIST.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyUIUserProfileValidRolesSuccess(){
		final UserProfile userProfile = getUIUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		final Hashtable<String, String> ctxtProps = new Hashtable<String,String>();
		ctxtProps.put(javax.naming.Context.SECURITY_PRINCIPAL,"uid=admin,"+USERS_BASE_DN);
		try {
			context.checking(new Expectations() {
	    		{
	    			one(mockedContext).rebind( with(equal(userProfileDN)),with(equal(userProfile)));
	    			one(mockedContext).getEnvironment();
	    			will(returnValue(ctxtProps));
	    		}
	    	});
			final UserProfileManagementMock2 mockedUserProfileMgmt =  new UserProfileManagementMock2(true,false);
	    	MESSAGES result = mockedUserProfileMgmt.modify(userProfile, mockedContext);
	    	Assert.assertEquals(result,MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyAdminUserProfileValidRolesSuccess(){
		final UserProfile userProfile = getSysAdminUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		final Hashtable<String, String> ctxtProps = new Hashtable<String,String>();
		ctxtProps.put(javax.naming.Context.SECURITY_PRINCIPAL,"uid=admin,"+USERS_BASE_DN);
		try {
			context.checking(new Expectations() {
	    		{
	    			one(mockedContext).rebind( with(equal(userProfileDN)),with(equal(userProfile)));
	    			one(mockedContext).getEnvironment();
	    			will(returnValue(ctxtProps));
	    		}
	    	});
			final UserProfileManagementMock2 mockedUserProfileMgmt =  new UserProfileManagementMock2(true,true);
	    	MESSAGES result = mockedUserProfileMgmt.modify(userProfile, mockedContext);
	    	Assert.assertEquals(result,MESSAGES.MSG_SUCCESS);
		} catch (LDAPException e) {
			Assert.fail("LDAPException not expected");
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyUserNamingException(){
		final UserProfile userProfile = getSysAdminUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		final Hashtable<String, String> ctxtProps = new Hashtable<String,String>();
		ctxtProps.put(javax.naming.Context.SECURITY_PRINCIPAL,"uid=admin,"+USERS_BASE_DN);
		try {
			context.checking(new Expectations() {
	    		{
	    			one(mockedContext).rebind( with(equal(userProfileDN)),with(equal(userProfile)));
	    			will(throwException(new NamingException()));
	    			one(mockedContext).getEnvironment();
	    			will(returnValue(ctxtProps));
	    		}
	    	});
			final UserProfileManagementMock2 mockedUserProfileMgmt =  new UserProfileManagementMock2(true,true);
	    	MESSAGES result = mockedUserProfileMgmt.modify(userProfile, mockedContext);
	    	Assert.fail("Expected LDAP Exception not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_DIRECTORY_SERVER_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	@Test
	public void testModifyUserGeneralException(){
		final UserProfile userProfile = getSysAdminUserProfileLDAPObject();
		final String userProfileDN = getUserProfileDN(id);
		final Hashtable<String, String> ctxtProps = new Hashtable<String,String>();
		ctxtProps.put(javax.naming.Context.SECURITY_PRINCIPAL,"uid=admin,"+USERS_BASE_DN);
		try {
			context.checking(new Expectations() {
	    		{
	    			one(mockedContext).getEnvironment();
	    			will(returnValue(ctxtProps));
	    			one(mockedContext).rebind( with(equal(userProfileDN)),with(equal(userProfile)));
	    			will(throwException(new NullPointerException()));
	    		}
	    	});
			final UserProfileManagementMock2 mockedUserProfileMgmt =  new UserProfileManagementMock2(true,true);
	    	MESSAGES result = mockedUserProfileMgmt.modify(userProfile, mockedContext);
	    	Assert.fail("Expected LDAP Exception not thrown");
		} catch (LDAPException e) {
			Assert.assertEquals(e.getErrorMessage(),MESSAGES.ERR_UNEXPECTED_EXCEPTION.getMessage());
		} catch (Exception e) {
			Assert.fail("Exception not expected");
		}
	}
	
	/**
	 * Create a test LDAP object for userprofile
	 * @return UserProfile LDAP Object
	 */
	private UserProfile getSysAdminUserProfileLDAPObject(){
		final UserProfileVO vo = new UserProfileVO();
		vo.setPredefined(false);
		vo.setUserId(id);
		final Set<String> roles = new HashSet<String>();
		roles.add(PREDEFINED_SYSADMIN_ROLE);
		vo.setRoles(roles);
		final UserProfile userProfile=  new UserProfile(vo);
		return userProfile;
	}
	
	/**
	 * Create a test LDAP object for userprofile
	 * @return UserProfile LDAP Object
	 */
	private UserProfile getUIUserProfileLDAPObject(){
		final UserProfileVO vo = new UserProfileVO();
		vo.setPredefined(false);
		vo.setUserId(id);
		final Set<String> roles = new HashSet<String>();
		roles.add("terminalspecialist");
		vo.setRoles(roles);
		final UserProfile userProfile=  new UserProfile(vo);
		return userProfile;
	}
	
	/** 
	 * Returns a User Profile  filter e.g. (&(objectclass=role)((cn=poweruser)))
	 * @param id a User Profile  id e.g. user.id 
	 * @return User Profile Filter
	 */
	private String getUserProfileFilterLong(final String id){
		final StringBuilder filter = new StringBuilder();
		filter.append(LEFT_BRACE);
		filter.append(OP_AND);
		filter.append(LEFT_BRACE);
		filter.append(OBJECT_CLASS);
		filter.append(OP_EQUALS);
		filter.append(USER_PROFILE);
		filter.append(RIGHT_BRACE);
		filter.append(LEFT_BRACE);
		filter.append(LEFT_BRACE);
		filter.append(UID);
		filter.append(OP_EQUALS);
		filter.append(id);
		filter.append(RIGHT_BRACE);
		filter.append(RIGHT_BRACE);
		filter.append(RIGHT_BRACE);
		return filter.toString();
	}
	
	/** 
	 * Returns a User Profile  filter e.g. (objectclass=role)
	 * @return User Profile Filter
	 */
	private String getUserProfileFilterShort(){
		final StringBuilder filter = new StringBuilder();
		filter.append(LEFT_BRACE);
		filter.append(OBJECT_CLASS);
		filter.append(OP_EQUALS);
		filter.append(USER_PROFILE);
		filter.append(RIGHT_BRACE);
		return filter.toString();
	}
	
	/**
	   * returns a filter: e.g. (&(objectclass=role)(|(cn=sysadmin)(cn=poweruser)))
	   * @param id
	   * @return roles filter
	   */
	  private String getRolesFilterLong(final Set<String> roles){
		  final StringBuffer filter = new StringBuffer();
		  filter.append(LEFT_BRACE);
		  filter.append(OP_AND);
		  filter.append(LEFT_BRACE);
		  filter.append(OBJECT_CLASS);
		  filter.append(OP_EQUALS);
		  filter.append(ROLE); 
		  filter.append(RIGHT_BRACE);
		  filter.append(LEFT_BRACE);
		  filter.append(OP_OR);
		  for(final String role : roles ){
			  filter.append(LEFT_BRACE);
			  filter.append(COMMON_NAME);
			  filter.append(OP_EQUALS);
			  filter.append(role);
			  filter.append(RIGHT_BRACE);
		  }
		  filter.append(RIGHT_BRACE);
		  filter.append(RIGHT_BRACE);
		  return filter.toString();
	  }
	
	private String getUserProfileDN(final String id) {
		final StringBuffer dn = new StringBuffer();
		dn.append(UID);
		dn.append(OP_EQUALS);
		dn.append(id);
		dn.append(OP_COMMA);
		dn.append(USERPROFILES_BASE_DN);
		return dn.toString();
	}

	/**
	 * Mock UserProfileManagement class to increase testability.
	 * @author eramano
	 *
	 */
	class UserProfileManagementMock extends UserProfileManagement{
		
		private final boolean isAdminuser;
		
		UserProfileManagementMock(final boolean isAdminUser) {
			this.isAdminuser = isAdminUser;
		}
		
		@Override
		protected void updateLoginProfiles(final String uid, final String loginRoleDN, final int operation,
				final DirContext conn) throws NamingException {
			// skip logic and just return
			return;
		}

		@Override
		protected boolean isAdminUser(final String userId, final DirContext conn)
				throws LDAPException, NamingException {
			return this.isAdminuser;
		}
	}
	
	/**
	 * Second Mock UserProfileManagement class to increase testability.
	 * @author eramano
	 *
	 */
	class UserProfileManagementMock2 extends UserProfileManagement{
		
		private final boolean isAdminUser;
		private final boolean isRolesValid;
		
		UserProfileManagementMock2(final boolean isRolesValid , final boolean isAdminUser) {
			this.isAdminUser = isAdminUser;
			this.isRolesValid = isRolesValid;
		}
		
		@Override
		protected boolean isAllRolesValid(final Set<String> roles, final DirContext conn) throws LDAPException {
			return this.isRolesValid;
		}
		
		@Override
		protected boolean isAdminUser(final String userId, final DirContext conn ) throws LDAPException,NamingException {
			return this.isAdminUser;
		}
		
		@Override
		protected void updateLoginProfiles(final String uid, final String loginRoleDN, final int operation,
				final DirContext conn) throws NamingException {
			// skip logic and just return
			return;
		}

	}
}
