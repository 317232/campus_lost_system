package com.campus.lostfound.notice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.domain.entity.Announcement;
import com.campus.lostfound.mapper.AnnouncementMapper;
import com.campus.lostfound.notice.dto.NoticeDTO;
import com.campus.lostfound.notice.service.AnnouncementService;
import com.campus.lostfound.security.SecurityUserUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final SecurityUserUtils securityUserUtils;

    public NoticeServiceImpl(AnnouncementMapper announcementMapper, SecurityUserUtils securityUserUtils) {
        this.announcementMapper = announcementMapper;
        this.securityUserUtils = securityUserUtils;
    }

    @Override
    public Page<NoticeDTO.NoticeResp> getNoticesPage(Integer pageNum, Integer pageSize, String keyword, Boolean published) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();

        if (published != null) {
            queryWrapper.eq(Announcement::getStatus, published ? "PUBLISHED" : "OFFLINE");
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w.like(Announcement::getTitle, keyword)
                    .or()
                    .like(Announcement::getContent, keyword));
        }

        queryWrapper.orderByDesc(Announcement::getCreateTime);

        Page<Announcement> noticePage = announcementMapper.selectPage(page, queryWrapper);
        Page<NoticeDTO.NoticeResp> dtoPage = new Page<>(
            noticePage.getCurrent(),
            noticePage.getSize(),
            noticePage.getTotal()
        );

        List<NoticeDTO.NoticeResp> dtoList = noticePage.getRecords()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public NoticeDTO.NoticeResp getNoticeById(Long id) {
        Announcement notice = announcementMapper.selectById(id);
        if (notice == null) {
            return null;
        }
        return convertToDto(notice);
    }

    @Override
    public void createNotice(NoticeDTO.CreateNoticeReq request) {
        Long publisherId = securityUserUtils.getCurrentUserId();
        if (publisherId == null) {
            throw new IllegalArgumentException("发布公告需要登录管理员身份");
        }
        Announcement notice = new Announcement();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPublisherId(publisherId);
        notice.setStatus(request.getPublished() != null && request.getPublished() ? "PUBLISHED" : "DRAFT");

        if (Boolean.TRUE.equals(request.getPublished())) {
            notice.setPublishedAt(LocalDateTime.now());
        }

        announcementMapper.insert(notice);
    }

    @Override
    public boolean updateNotice(NoticeDTO.UpdateNoticeReq request) {
        if (request.getId() == null) {
            return false;
        }

        Announcement notice = announcementMapper.selectById(request.getId());
        if (notice == null) {
            return false;
        }

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());

        if (request.getPublished() != null) {
            boolean wasPublished = "PUBLISHED".equals(notice.getStatus());
            boolean nowPublished = Boolean.TRUE.equals(request.getPublished());

            notice.setStatus(nowPublished ? "PUBLISHED" : "OFFLINE");

            if (!wasPublished && nowPublished) {
                notice.setPublishedAt(LocalDateTime.now());
            }
        }

        return announcementMapper.updateById(notice) > 0;
    }

    @Override
    public boolean deleteNotice(Long id) {
        return announcementMapper.deleteById(id) > 0;
    }

    private NoticeDTO.NoticeResp convertToDto(Announcement notice) {
        NoticeDTO.NoticeResp dto = new NoticeDTO.NoticeResp();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setPublished("PUBLISHED".equals(notice.getStatus()));
        dto.setPublishedAt(notice.getPublishedAt());
        dto.setCreateTime(notice.getCreateTime());
        return dto;
    }
}