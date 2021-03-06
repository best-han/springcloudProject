package com.windaka.suizhi.user.service.impl;

import cn.hutool.core.map.MapUtil;
import com.windaka.suizhi.api.common.Page;
import com.windaka.suizhi.api.common.ReturnConstants;
import com.windaka.suizhi.api.oss.sys.HtMenu;
import com.windaka.suizhi.api.oss.sys.HtPermission;
import com.windaka.suizhi.api.user.AppUser;
import com.windaka.suizhi.api.user.LoginAppUser;
import com.windaka.suizhi.api.user.Role;
import com.windaka.suizhi.api.user.UserCredential;
import com.windaka.suizhi.api.user.constants.CredentialType;
import com.windaka.suizhi.common.exception.OssRenderException;
import com.windaka.suizhi.common.utils.*;
import com.windaka.suizhi.user.dao.*;
import com.windaka.suizhi.user.dao.auto.*;
import com.windaka.suizhi.user.dao.ext.ExtHtUserMapper;
import com.windaka.suizhi.user.model.*;
import com.windaka.suizhi.user.model.ext.ExtHtUser;
import com.windaka.suizhi.user.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppUserServiceImpl implements AppUserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserCredentialsDao userCredentialsDao;
    @Autowired
    private AppUserDao appUserDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private UserAreaDao userAreaDao;
    @Autowired
    private RolePermissionDao rolePermissionDao;
    @Autowired
    private UserMenuDao userMenuDao;
    @Autowired
    private UserPermissionDao userPermissionDao;
    @Autowired
    private UserXqDao userXqDao;
    @Autowired
    private SubdistrictDao subdistrictDao;
    @Autowired
    private HtUserMapper htUserMapper;
    @Autowired
    private HtAreaMapper htAreaMapper;
    @Autowired
    private HtSubdistrictInfoMapper htSubdistrictInfoMapper;
    @Autowired
    private HtUserXqMapper htUserXqMapper;
    @Autowired
    private BaseCommunityMapper baseCommunityMapper;
    @Autowired
    private HtXqSubdistrictMapper htXqSubdistrictMapper;
    @Autowired
    private HtUserSubdistrictMapper htUserSubdistrictMapper;
    @Autowired
    private ExtHtUserMapper extHtUserMapper;


    //@LogAnnotation(module = "新增用户")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveAppUser(AppUser appUser, Set<String> roleIds) throws OssRenderException {
        String username = appUser.getUsername();
        String cname = appUser.getCname();
        String phone = appUser.getPhone();
        if (StringUtils.isBlank(username)) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "用户名不能为空");
        }
        //验证用户名格式
      /*  if (!AppUserUtil.checkUsername(username)){
            throw new OssRenderException(ReturnConstants.CODE_FAILED,"用户名必须以英文字符开头，并且只能是字母数字组合");
        }*/

        if (username.length() > 30) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "用户名不能超过30位");
        }

        if (StringUtils.isBlank(appUser.getPassword())) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "密码不能为空");
        }

        if (appUser.getPassword().length() > 60) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "密码不能超过60位");
        }

        if (!StringUtils.isBlank(cname) && cname.length() > 20) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "姓名不能超过20字");
        }

        if (!StringUtils.isBlank(phone) && !PhoneUtil.checkPhone(phone)) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "请收入正确格式的手机号");
        }

        UserCredential userCredential = userCredentialsDao.queryUserCredentialByUsername(appUser.getUsername());
        if (userCredential != null) {
            throw new OssRenderException(ReturnConstants.CODE_ADD_USER_BAD, "用户名已存在");
        }
        if (appUser.getSysLevel() == null || StringUtils.isBlank(appUser.getSysLevel())) {//用户级别（1：超管，2：区级管理员，3：小区普通用户，4：街道管理员）
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "请选择用户级别");
        }
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        appUser.setUserId(Tools.getUUID()); //生成用户UUID主键
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword())); // 加密密码
        appUser.setEnabled(Boolean.TRUE);
        appUser.setCreBy(loginAppUser.getUserId());    //记录操作人
        appUser.setCreTime(new Date());
        appUser.setUpdTime(appUser.getCreTime());
        appUser.setUpdBy(loginAppUser.getUserId());
        //如果没有获取到区域ID，设置当前登录用户ID
     /*   if(appUser.getWyCode() == null){
            appUser.setWyCode(loginAppUser.getWyCode());   //当前登录用户所属物业
        }
        if(appUser.getXqCode() == null){
            appUser.setXqCode(loginAppUser.getXqCode());   //当前登录用户所属小区
        }*/


        //如果没有获取到超级管理员标识，默认为 false
        if (appUser.getSysAdmin() == null) {
            appUser.setSysAdmin(false);
        }
        //添加用户
        appUserDao.saveAppUser(appUser);

        //添加用户凭证表
        userCredentialsDao.saveUserCredential(
                new UserCredential(appUser.getUsername(), CredentialType.USERNAME.name(), appUser.getUserId()));

        if (roleIds != null && roleIds.size() > 0) {
            userRoleDao.saveUserRoles(appUser.getUserId(), roleIds);
        }
        return appUser.getUserId();
    }

    /**
     * 功能描述: 添加用户信息
     *
     * @auther: lixianhua
     * @date: 2020/4/22 9:14
     * @param:
     * @return:
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(ExtHtUser user) throws OssRenderException {
        HtUser model = new HtUser();
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        BeanUtils.copyProperties(user, model);
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword()) || StringUtils.isBlank(user.getCname()) || StringUtils.isBlank(user.getSysLevel())) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "存在必填项未填写");
        }
        HtUserExample example = new HtUserExample();
        HtUserExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(user.getUsername());
        List<HtUser> list = this.htUserMapper.selectByExample(example);
        if (null != list && list.size() > 0) {
            throw new OssRenderException(ReturnConstants.CODE_ADD_USER_BAD, "用户名已存在");
        }
        HtArea area = this.htAreaMapper.selectByPrimaryKey(user.getAreaId());
        if (null == area) {
            throw new OssRenderException(ReturnConstants.CODE_ADD_USER_BAD, "用户未选择区");
        }
        model.setAreaName(area.getAreaName());
        if ("2".equals(user.getSysLevel())) {// 区级管理员
            if (null == user.getAreaId()) {
                throw new OssRenderException(ReturnConstants.CODE_ADD_USER_BAD, "区用户未选择区");
            }
        } else {
            HtSubdistrictInfo subInfo = this.htSubdistrictInfoMapper.selectByPrimaryKey(user.getSubdistrictId());
            if (null == subInfo) {
                throw new OssRenderException(ReturnConstants.CODE_ADD_USER_BAD, "用户未选择街道");
            }
            model.setSubdistrictName(subInfo.getName());
            if ("4".equals(user.getSysLevel())) {// 街道用户
                if (null == user.getSubdistrictId()) {
                    throw new OssRenderException(ReturnConstants.CODE_ADD_USER_BAD, "街道用户未选择街道");
                }
            } else if ("3".equals(user.getSysLevel())) {// 普通用户
                if (StringUtils.isBlank(user.getXqCodes())) {
                    throw new OssRenderException(ReturnConstants.CODE_ADD_USER_BAD, "普通用户未选择小区");
                }
            }
        }
        model.setPassword(passwordEncoder.encode(model.getPassword())); // 加密密码
        model.setEnabled(true);
        model.setUserId(Tools.getUUID());
        model.setCreTime(new Date());
        model.setCreBy(loginAppUser.getUserId());
        model.setUpdBy(loginAppUser.getUserId());
        model.setUpdTime(new Date());
        int num = this.htUserMapper.insertSelective(model);
        if (num > 0) {
            //添加用户凭证表
            userCredentialsDao.saveUserCredential(
                    new UserCredential(model.getUsername(), CredentialType.USERNAME.name(), model.getUserId()));
            if ("3".equals(user.getSysLevel())) {// 普通（绑定选中的小区）
                List<String> xqList = Arrays.asList(user.getXqCodes().split(","));
                if (null != xqList && xqList.size() > 0) {
                    for (String xqCode : xqList) {
                        HtUserXq userXq = new HtUserXq();
                        userXq.setStatus("0");
                        userXq.setUserId(model.getUserId());
                        userXq.setXqCode(xqCode);
                        this.htUserXqMapper.insertSelective(userXq);
                    }
                }
            }
        }
        return num;
    }

    /**
     * 功能描述: 根据userid获取用户信息
     *
     * @auther: lixianhua
     * @date: 2020/4/22 14:01
     * @param:
     * @return:
     */
    @Override
    public ExtHtUser getUserByUserId(String userId) {
        ExtHtUser user = new ExtHtUser();
        HtUserExample example = new HtUserExample();
        example.createCriteria().andUserIdEqualTo(userId);
        HtUser htUser = this.htUserMapper.selectByExample(example).get(0);
        BeanUtils.copyProperties(htUser, user);
        if ("3".equals(htUser.getSysLevel())) {
            // 获取用户下的小区
            HtUserXqExample xqExample = new HtUserXqExample();
            xqExample.createCriteria().andUserIdEqualTo(userId);
            List<HtUserXq> userXqs = this.htUserXqMapper.selectByExample(xqExample);
            // 将集合转成数组
            List<String> codeList = new ArrayList<>();
            if (null != userXqs && userXqs.size() > 0) {
                for (HtUserXq userXq : userXqs) {
                    codeList.add(userXq.getXqCode());
                }
            }
            user.setCodeList(codeList);
        }
        return user;
    }

    /**
     * 功能描述: 获取用户集合
     *
     * @auther: lixianhua
     * @date: 2020/4/22 15:19
     * @param:
     * @return:
     */
    @Override
    public List<ExtHtUser> getUserList(ExtHtUser extHtUser) {
        if (StringUtils.isNotBlank(extHtUser.getStartTime())) {
            extHtUser.setStartDate(TimesUtil.stringToDate(extHtUser.getStartTime(), TimesUtil.DATE_TIME_FORMAT));
        }
        if (StringUtils.isNotBlank(extHtUser.getEndTime())) {
            extHtUser.setEndDate(TimesUtil.stringToDate(extHtUser.getEndTime(), TimesUtil.DATE_TIME_FORMAT));
        }
        List<ExtHtUser> list = this.extHtUserMapper.getUserList(extHtUser);
        return list;
    }

    /**
     * 功能描述: 根据街道获取小区信息
     *
     * @auther: lixianhua
     * @date: 2020/4/22 16:59
     * @param:
     * @return:
     */
    @Override
    public List<BaseCommunity> getXqBySub(String subdistrictId) {
        List<BaseCommunity> baseList = new ArrayList<>();
        HtXqSubdistrictExample example = new HtXqSubdistrictExample();
        example.createCriteria().andSubdistrictIdEqualTo(subdistrictId);
        List<HtXqSubdistrict> list = this.htXqSubdistrictMapper.selectByExample(example);
        if (null != list && list.size() > 0) {
            for (HtXqSubdistrict sub : list) {
                BaseCommunityExample example1 = new BaseCommunityExample();
                example1.createCriteria().andCodeEqualTo(sub.getXqCode());
                List<BaseCommunity> commList = this.baseCommunityMapper.selectByExample(example1);
                if (null != commList && commList.size() > 0) {
                    baseList.add(commList.get(0));
                }
            }
        }
        return baseList;
    }

    //@LogAnnotation(module = "修改个人信息")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String updateAppUser(AppUser appUser, Set<String> roleIds) throws OssRenderException {
        if (appUser == null) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "修改的用户不存在");
        }
        if (StringUtils.isBlank(appUser.getUserId()) && StringUtils.isBlank(appUser.getUsername())) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "修改的用户不存在");
        }
        AppUser oldAppUser = null;
        if (!StringUtils.isBlank(appUser.getUserId())) {
            oldAppUser = appUserDao.queryByUserIdForAppUser(appUser.getUserId());
        } else {
            oldAppUser = appUserDao.queryByUsername(appUser.getUsername());
            appUser.setUserId(oldAppUser.getUserId());
        }
        if (oldAppUser == null) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "修改的用户不存在");
        }
        appUser.setSysAdmin(oldAppUser.getSysAdmin());
        String cname = appUser.getCname();
        String phone = appUser.getPhone();

        if (!StringUtils.isBlank(cname) && cname.length() > 20) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "姓名不能超过20字");
        }

        if (!StringUtils.isBlank(phone) && !PhoneUtil.checkPhone(phone)) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "请收入正确格式的手机号");
        }

        appUser.setUpdTime(new Date());
        String pwd = appUser.getPassword();
        System.out.println("加密前password..." + appUser.getPassword());
        if (pwd == null || pwd.trim().length() == 0) {
            appUser.setPassword(null);
        } else {
            appUser.setPassword(passwordEncoder.encode(pwd));
        }
        System.out.println("加密后password..." + appUser.getPassword());
        //执行修改
        appUserDao.updateAppUserByUserId(appUser);
        userRoleDao.deleteUserRole(appUser.getUserId());

        if (roleIds != null && roleIds.size() > 0) {
            userRoleDao.saveUserRoles(appUser.getUserId(), roleIds);
        }
        return appUser.getUserId();

    }

    /**
     * @Author: hjt
     * @Date: 2018/12/18
     * @Description: 删除OSS用户-伪删除
     */
    //@LogAnnotation(module = "删除用户")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteAppUser(String userId) throws OssRenderException {
        AppUser appUser = new AppUser();
        AppUser oldAppUser = appUserDao.queryByUserIdForAppUser(userId);
        if (oldAppUser == null) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "删除的用户不存在");
        }
        //获取当前操作用户
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        appUser.setUserId(userId);
        appUser.setUpdBy(loginAppUser.getUserId());
        appUser.setUpdTime(new Date());
        int a = appUserDao.deleteAppUser(appUser);
        userRoleDao.deleteUserRole(userId);
        if (a <= 0) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "用户删除失败，请稍后再试！");
        }

    }

    //@LogAnnotation(module = "通过username查询用户信息")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginAppUser queryByUsername(String username, String password) {
        //首先获取用户凭证
        UserCredential userCredential = userCredentialsDao.queryUserCredentialByUsername(username);

        AppUser appUser = null;
        //用户凭证不为空，则查询用户信息
        if (userCredential != null) {
            appUser = appUserDao.queryByUserIdForAppUser(userCredential.getUserId());
        }

        //如果用户不存在返回空即可
        if (appUser == null) {
            return null;
        }

        //如果后期有非用户名密码登录的情况，则不需要传密码
        if (!StringUtils.isBlank(password)) {
            //如果密码校验不通过，不需要后续权限的设置，直接返回到null即可
            if (!passwordEncoder.matches(password, appUser.getPassword())) {
                return null;
            }
        }

        LoginAppUser loginAppUser = new LoginAppUser();
        BeanUtils.copyProperties(appUser, loginAppUser);

        //查询用户的角色
        Set<Role> roles = userRoleDao.queryRolesByUserIdForRole(appUser.getUserId());
        //将用户的角色信息放入登录信息中
        loginAppUser.setRoles(roles);
     /*       Area area = null;
        //用户的区域ID不为空，则查询并放入登录信息总
     if(!StringUtils.isBlank(appUser.getAreaId())){
            //用户的区域id如果为OssAdmin.AREA_ID代表是系统超级管理员，不属于真实区域
            if(!OssAdmin.AREA_ID.getAreaId().equals(appUser.getAreaId())) {
                area = userAreaDao.queryByAreaId(appUser.getAreaId());
                //区域状态部不为1时，区域被停止，禁止登陆
                if (area.getStatus() != 1) {
                    return null;
                }
                loginAppUser.setArea(area);
            }
        }*/
      /*  WyAndXqInfo wyAndXqInfo=new WyAndXqInfo();
        wyAndXqInfo.setWyCode(appUser.getWyCode());
        wyAndXqInfo.setXqCode(appUser.getXqCode());
        loginAppUser.setWyAndXqInfo(wyAndXqInfo);*/
        loginAppUser.setSysAdmin(appUser.getSysAdmin());
        loginAppUser.setSysLevel(appUser.getSysLevel());

        //查询用户操作权限
        Set<HtPermission> permissions = null;


        //超级管理员查询所有按钮权限
        if (appUser.getSysAdmin()) {
            permissions = userPermissionDao.queryAdminPermissions();
        } else {//其他用户根据角色权限查询即可
            if (!CollectionUtils.isEmpty(roles)) {
                Set<String> roleIds = roles.parallelStream().map(Role::getRoleId).collect(Collectors.toSet());
                permissions = rolePermissionDao.queryPermissionsByRoleIds(roleIds);
            }
        }

        //将操作权限信息放入登录信息
        loginAppUser.setPermissions(permissions);
        return loginAppUser;

    }

    //@LogAnnotation(module = "根据userId查询用户")
    @Override
    public Map<String, Object> queryByUserId(String userId) throws OssRenderException {
        Map<String, Object> userMap = appUserDao.queryByUserId(userId);
        if (userMap == null) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "查询用的用户不存在");
        }

        //不是区域管理员，需要查询用户角色信息
        //  if(!"1".equals(userMap.get("sysAdmin").toString())){
        List list = userRoleDao.queryRolesByUserId(userId);
        userMap.put("roles", list);
        List<String> xqList = userRoleDao.queryXqCodeByUserId(userId);
        userMap.put("xqCodes", xqList);
        //  }
      /*  Map<String,Object> roleMap=userRoleDao.queryRoleByUserId(userId);
        userMap.put("roleId",roleMap.get("roleId"));
        userMap.put("roleName",roleMap.get("roleName"));*/
        return userMap;
    }

    @Override
    public AppUser queryByUserIdForAppUser(String userId) throws OssRenderException {
        return appUserDao.queryByUserIdForAppUser(userId);
    }

    /**
     * 给用户设置角色<br>
     * 这里采用先删除老角色，再插入新角色
     */
    //@LogAnnotation(module = "分配角色")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveRoleToUser(String userId, Set<String> roleIds) throws OssRenderException {
        AppUser appUser = appUserDao.queryByUserIdForAppUser(userId);
        //判断用户是否存在
        this.userIsExists(appUser);
        //先删除老角色
        userRoleDao.deleteUserRole(userId);
        if (!CollectionUtils.isEmpty(roleIds)) {
            userRoleDao.saveUserRoles(userId, roleIds);//再插入新角色
        }

        log.info("修改用户：{}的角色，{}", userId, roleIds);
    }

    //@LogAnnotation(module = "修改密码")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePassword(String userId, String oldPassword, String newPassword) throws OssRenderException {

        AppUser appUser = appUserDao.queryByUserIdForAppUser(userId);
        //密码重置时，oldPassword为空
        if (StringUtils.isNoneBlank(oldPassword)) {
            if (!passwordEncoder.matches(oldPassword, appUser.getPassword())) { // 旧密码校验
                throw new OssRenderException(ReturnConstants.CODE_FAILED, "旧密码错误");
            }
        }
        if (StringUtils.isBlank(newPassword)) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "新密码不能为空");
        }
        if (newPassword.length() > 60) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "新密码不能超过60位");
        }
        if (appUser == null) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "修改的用户不存在");
        }
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        appUser.setPassword(passwordEncoder.encode(newPassword)); // 加密密码
        appUser.setUpdBy(loginAppUser.getUserId());
        appUser.setUpdTime(new Date());
        //执行修改
        appUserDao.updateAppUserByUserId(appUser);
        log.info("修改密码：{}", appUser);

    }

    //@LogAnnotation(module = "用户列表查询")
    @Override
    public Page<Map<String, Object>> queryList(Map<String, Object> params) throws OssRenderException {
        //当前登录管理员所在物业
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        //当前登录用户ID
        //    params.put("wyCode",loginAppUser.getWyCode());
        //当前开始和结束日期是否正确
        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        if (!StringUtils.isBlank(startTime)) {
            if (TimesUtil.checkDateFormat(startTime)) {
                //params.put("startTime",startTime+" 00:00:00");
            } else {
                throw new OssRenderException(ReturnConstants.CODE_FAILED, "开始时间格式不正确");
            }
        }
        if (!StringUtils.isBlank(endTime)) {
            if (TimesUtil.checkDateFormat(endTime)) {
                //params.put("endTime",endTime+" 23:59:59");
            } else {
                throw new OssRenderException(ReturnConstants.CODE_FAILED, "结束时间格式不正确");
            }
        }

        params.put("creBy", loginAppUser.getUserId());//只能查看当前登录人创建的用户（控制权限）
        int totalRows = appUserDao.totalRows(params);
        List<Map<String, Object>> list = Collections.emptyList();
        if (totalRows > 0) {
            PageUtil.pageParamConver(params, true);

            list = appUserDao.queryList(params);
        }
        //公安局展示放入管辖区域start
        for (Map map : list) {
            List<Map<String, Object>> subdistrictList = subdistrictDao.querySubdistrictByUserId((String) map.get("userId"));
            String subdistrictName = "";
            for (Map mapSubdistrict : subdistrictList) {
                subdistrictName += mapSubdistrict.get("subdistrictName") + "、";
            }
            if (subdistrictName.length() > 0) {
                subdistrictName = subdistrictName.substring(0, subdistrictName.length() - 1);
            }
            map.put("subdistrictName", subdistrictName);
        }//end

        return new Page<>(totalRows, MapUtils.getInteger(params, PageUtil.PAGE), list);
    }

    public static void main(String args[]) {
        String subdistrictName = "";
        System.out.println(subdistrictName.substring(0, subdistrictName.length() - 1));
    }

    @Override
    public Set<Role> queryRolesByUserIdForRole(String userId) throws OssRenderException {
        Set<Role> roles = null;
        try {
            roles = userRoleDao.queryRolesByUserIdForRole(userId);
        } catch (Exception e) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, ReturnConstants.MSG_QUERY_FAILED);
        }
        return roles;
    }

    /**
     * 获取当前用户角色的菜单
     *
     * @param userId
     * @return
     */
    //@LogAnnotation(module = "获取当前用户角色的菜单-普通用户")
    @Override
    public List<HtMenu> queryUserRoleMenus(String userId) throws OssRenderException {
        return userMenuDao.queryUserRoleMenus(userId);
    }

    /**
     * 获取当前用户的菜单-管理员用
     *
     * @param
     * @return
     */
    //@LogAnnotation(module = "获取当前用户的菜单-超级管理员")
    @Override
    public List<HtMenu> queryAdminMenus() throws OssRenderException {
        return userMenuDao.queryAdminMenus();
    }

    /**
     * @Author: hjt
     * @Date: 2018/12/25
     * @Description: 根据当前菜单menuId查询当前菜单可用按钮
     */
    //@LogAnnotation(module = "根据当前登录用户可用按钮")
    @Override
    public Set<HtPermission> queryLoginUserPermissions() {
        //获取登录时保存功能按钮集合
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        Set<HtPermission> permissionSet = loginAppUser.getPermissions();
        return permissionSet;
    }

    /**
     * @Author: hjt
     * @Description: 修改当前登录用户的昵称和手机号
     */
    //@LogAnnotation(module = "修改个人信息")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginAppUser updateMe(Map<String, Object> map) throws OssRenderException {

        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();

        AppUser appUser = appUserDao.queryByUserIdForAppUser(loginAppUser.getUserId());
        appUser.setSysAdmin(appUser.getSysAdmin());
        String cname = (String) map.get("cname");
        String phone = (String) map.get("phone");

        if (!StringUtils.isBlank(cname) && cname.length() > 20) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "姓名不能超过20字");
        }

        if (!StringUtils.isBlank(phone) && !PhoneUtil.checkPhone(phone)) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "请收入正确格式的手机号");
        }
        appUser.setUpdTime(new Date());
        appUser.setCname(cname);
        appUser.setPhone(phone);
        //执行修改
        int a = appUserDao.updateAppUserByUserId(appUser);
        if (a <= 0) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "用户修改失败，请稍后再试！");
        } else {
            BeanUtils.copyProperties(appUser, loginAppUser);
            return loginAppUser;
        }

    }

    /**
     * 判断用户是否存在
     *
     * @param appUser
     * @throws OssRenderException
     */
    public void userIsExists(AppUser appUser) throws OssRenderException {
        if (appUser == null) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "用户不存在");
        }
    }

    /**
     *
     * @param userId
     * @param phone

     @Transactional
     @Override public void bindingPhone(String userId, String phone) throws OssRenderException {
     UserCredential userCredential = userCredentialsDao.queryUserCredentialByUsername(phone);
     if (userCredential != null) {
     throw new IllegalArgumentException("手机号已被绑定");
     }

     AppUser appUser = appUserDao.queryByUserId(userId);
     appUser.setPhone(phone);

     updateAppUser(appUser);
     log.info("绑定手机号成功,username:{}，phone:{}", appUser.getUsername(), phone);

     // 绑定成功后，将手机号存到用户凭证表，后续可通过手机号+密码或者手机号+短信验证码登陆
     userCredentialsDao.saveUserCredential(new UserCredential(phone, CredentialType.PHONE.name(), userId));
     }
     */
    /**
     * 根据parentId查询下级区域信息
     *
     * @param parentId
     * @return
     * @throws OssRenderException
     */
    @Override
    public List<Map<String, Object>> queryAreaInfoByPid(String parentId) throws OssRenderException {
        List<Map<String, Object>> list = userAreaDao.queryAreaInfoByPid(parentId);
        if (Objects.isNull(list) || list.size() == 0) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "该地区无下级区域");
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveXqToUser(String userId, Set<String> xqCodes) throws OssRenderException {
        AppUser appUser = appUserDao.queryByUserIdForAppUser(userId);
        //判断用户是否存在
        this.userIsExists(appUser);
        //先删除之前分配的小区
        userXqDao.deleteUserXq(userId);
        if (!CollectionUtils.isEmpty(xqCodes)) {
            userXqDao.saveUserXq(userId, xqCodes);//再插入新小区
        }
        log.info("修改用户的小区：{}的小区，{}", userId, xqCodes);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveXqToUserByUsername(String username, Set<String> xqCodes) throws OssRenderException {
        AppUser appUser = appUserDao.queryByUsername(username);
        //判断用户是否存在
        this.userIsExists(appUser);
        //先删除之前分配的小区
        //userXqDao.deleteUserXq(userId);
        String userId = appUser.getUserId();
        if (!CollectionUtils.isEmpty(xqCodes)) {
            List<Map<String, Object>> list = userXqDao.queryUserXqList(userId, xqCodes);
            if (list != null && list.size() > 0) {
                throw new OssRenderException(ReturnConstants.CODE_FAILED, "有小区已绑定");
            }
            userXqDao.saveUserXq(userId, xqCodes);//再插入新小区
        }
        log.info("绑定用户和该小区：{}的小区，{}", username, xqCodes);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteXqUserByUsername(String username, Set<String> xqCodes) throws OssRenderException {
        AppUser appUser = appUserDao.queryByUsername(username);
        //判断用户是否存在
        this.userIsExists(appUser);
        //先删除之前分配的小区
        //userXqDao.deleteUserXq(userId);
        String userId = appUser.getUserId();
        if (!CollectionUtils.isEmpty(xqCodes)) {
            userXqDao.deleteUserXqByXqCodes(userId, xqCodes);
        }
        log.info("解除绑定用户和这些小区：{}的小区，{}", username, xqCodes);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUserSubdistrict(Map<String, Object> params) throws OssRenderException {
        if (MapUtil.isEmpty(params) || params.get("userId") == null || params.get("subdistrictIds") == null) {
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "请传入值");
        }
        String userId = (String) params.get("userId");
        subdistrictDao.deleteUserSubdistrict(userId);
        subdistrictDao.saveUserSubdistrict(params);
    }

    @Autowired
    private SubdistrictXqDao subdistrictXqDao;

    @Override
    public List<Map<String, Object>> queryAreaToXq() throws OssRenderException {
        LoginAppUser loginAppUser = AppUserUtil.getLoginAppUser();
        String sysLevel = loginAppUser.getSysLevel();
        List<Map<String, Object>> list = new ArrayList<>();
        if ("1".equals(sysLevel)) {//超管
            list = subdistrictDao.queryAllAreaBySubdistrictInfo();
        }
        if ("2".equals(sysLevel)) {//区级管理员
            list = userAreaDao.queryAreaInfoByUserId(loginAppUser.getUserId());
        }
        if ("4".equals(sysLevel)) {//街道级管理员
            list = subdistrictDao.queryUserAreaBySubdistrict(loginAppUser.getUserId());
        }
        for (Map map : list) {
            List<Map<String, Object>> subdistrictList = subdistrictDao.querySubdistrictByAreaId(map.get("areaId").toString());
            for (Map subdistrictMap : subdistrictList) {
                //查该街道下所有有小区
                List<Map<String, Object>> xqList = subdistrictXqDao.queryXqBySubdistrictId(subdistrictMap.get("subdistrictId").toString());
                subdistrictMap.put("xqList", xqList);
            }
            map.put("subdistricts", subdistrictList);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> querySubdistrictByAreaId(String areaId) throws OssRenderException {
        List<Map<String, Object>> list = subdistrictDao.querySubdistrictByAreaId(areaId);
        /*if(Objects.isNull(list) || list.size()==0){
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "该区域无街道");
        }*/
        return list;
    }

    @Override
    public List<Map<String, Object>> queryXqBySubdistrictId(String areaId) throws OssRenderException {
        List<Map<String, Object>> list = subdistrictDao.queryXqBySubdistrictId(areaId);
        /*if(Objects.isNull(list) || list.size()==0){
            throw new OssRenderException(ReturnConstants.CODE_FAILED, "该街道无小区");
        }*/
        return list;
    }

}
