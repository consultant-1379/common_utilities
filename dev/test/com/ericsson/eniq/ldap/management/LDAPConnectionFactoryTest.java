package com.ericsson.eniq.ldap.management;

import org.junit.Test;

/**
 * 
 * @author eramano
 *
 */
public class LDAPConnectionFactoryTest {
	
	@Test(expected=Exception.class)
	public void testGetSimpleConnectionFailure() throws Exception{
		LDAPConnectionFactory.getConnection("INVALIDUSER1234", "INVALIDPASSWORD1234");
	}
	
	/*@Test
	public void testModify(){
		final UserVO userVo = new UserVO(); //

		userVo.setFname("App1");
		userVo.setLname("Admin1");
		userVo.setPassword("UNCHANGED");
		userVo.setUserId("admin");
		userVo.setEmail("anoj@ljk.com");
		userVo.setPhone("");
		userVo.setPredefined(true);
		Set<String> roles = new HashSet<String>();
		roles.add("sysadmin");
		userVo.setRoles(roles);
		
		final LoginVO loginVo = new LoginVO();
		loginVo.setLoginId("admin");
		loginVo.setPassword("admin");
		
		IHandler handler = new UserHandler();
		try {
			//handler.modify(loginVo, userVo);
			UserVO vo = (UserVO)handler.findById(loginVo, userVo);
			System.out.println(vo.getPhone());
			
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/

}
