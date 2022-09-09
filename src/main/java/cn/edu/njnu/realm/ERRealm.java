package cn.edu.njnu.realm;

import cn.edu.njnu.mapper.RoleMapper;
import cn.edu.njnu.pojo.User;
import cn.edu.njnu.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

public class ERRealm extends AuthorizingRealm {

    @Autowired
    UserService userService;
    @Autowired
    RoleMapper roleMapper;
    /**
     * @description:  授权（用户进行权限验证时候Shiro会去缓存中找,如果查不到数据,会执行这个方法去查权限,并放入缓存中）
     * @author Madoor
     * @date 2022/9/8 19:02
     * */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //获取用户权限
        String userName = (String) principalCollection.getPrimaryPrincipal();
        String roleName = roleMapper.getRoleNameByUserName(userName);
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.addRole(roleName);
        return simpleAuthorizationInfo;
    }

    // 获取认证信息，即根据 token 中的用户名从数据库中获取密码、盐等并返回
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String username = token.getPrincipal().toString();
        User user = userService.getByName(username);
        if (ObjectUtils.isEmpty(user)) {
            throw new UnknownAccountException();
        }
        String passwordInDB = user.getUserPassword();
        String salt = user.getSalt();
        return new SimpleAuthenticationInfo(username, passwordInDB, ByteSource.Util.bytes(salt), getName());
    }
}
