package com.campus.lostfound.notice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.notice.dto.NoticeDTO;

public interface NoticeService {
    Page<NoticeDTO.NoticeResp> getNoticesPage(Integer pageNum, Integer pageSize, String keyword, Boolean published);

    NoticeDTO.NoticeResp getNoticeById(Long id);

    void createNotice(NoticeDTO.CreateNoticeReq request);

    boolean updateNotice(NoticeDTO.UpdateNoticeReq request);

    boolean deleteNotice(Long id);
}