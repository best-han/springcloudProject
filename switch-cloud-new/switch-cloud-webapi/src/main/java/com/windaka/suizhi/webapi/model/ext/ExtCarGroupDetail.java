package com.windaka.suizhi.webapi.model.ext;

import com.windaka.suizhi.webapi.model.CarGroupDetail;
import com.windaka.suizhi.webapi.model.HtUser;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName ExtCarGroupDetail
 * @Description 布控车辆实体类
 * @Author lixianhua
 * @Date 2020/4/16 11:39
 * @Version 1.0
 */
@Data
public class ExtCarGroupDetail extends CarGroupDetail {

    // 开始时间
    private String startTimeStr;

    // 结束时间
    private String endTimeStr;

    // 撤控时间
    private String withdrawTimeStr;

    // 撤控开始时间
    private Date withdrawStart;

    // 撤控开始时间字符串
    private String withdrawStartStr;

    // 撤控结束时间字符串
    private String withdrawEndStr;

    // 撤控结束时间
    private Date withdrawEnd;

    // 布控时间段
    private String controlPeriods;

    // 人员集合
    private List<HtUser> userList;

    // 是否分页
    private Boolean pageFlag;

}
