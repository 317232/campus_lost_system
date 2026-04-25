package com.campus.lostfound.notice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.domain.entity.Notice;
import com.campus.lostfound.notice.dto.NoticeDTO;
import com.campus.lostfound.notice.service.NoticeService;
import com.campus.lostfound.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public Page<NoticeDTO.NoticeResp> getNoticesPage(Integer pageNum, Integer pageSize, String keyword, Boolean published) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> queryWrapper = new LambdaQueryWrapper<>();

        if (published != null) {
            queryWrapper.eq(Notice::getPublished, published);
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(w -> w.like(Notice::getTitle, keyword)
                    .or()
                    .like(Notice::getContent, keyword));
        }

        queryWrapper.orderByDesc(Notice::getCreatedAt);

        Page<Notice> noticePage = noticeMapper.selectPage(page, queryWrapper);
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
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            return null;
        }
        return convertToDto(notice);
    }

    @Override
    public void createNotice(NoticeDTO.CreateNoticeReq request) {
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPublished(request.getPublished() != null ? request.getPublished() : false);

        if (Boolean.TRUE.equals(request.getPublished())) {
            notice.setPublishedAt(LocalDateTime.now());
        }

        noticeMapper.insert(notice);
    }

    @Override
    public boolean updateNotice(NoticeDTO.UpdateNoticeReq request) {
        if (request.getId() == null) {
            return false;
        }

        Notice notice = noticeMapper.selectById(request.getId());
        if (notice == null) {
            return false;
        }

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());

        if (request.getPublished() != null) {
            boolean wasPublished = Boolean.TRUE.equals(notice.getPublished());
            boolean nowPublished = Boolean.TRUE.equals(request.getPublished());

            notice.setPublished(request.getPublished());

            if (!wasPublished && nowPublished) {
                notice.setPublishedAt(LocalDateTime.now());
            }
        }

        return noticeMapper.updateById(notice) > 0;
    }

    @Override
    public boolean deleteNotice(Long id) {
        return noticeMapper.deleteById(id) > 0;
    }

    private NoticeDTO.NoticeResp convertToDto(Notice notice) {
        NoticeDTO.NoticeResp dto = new NoticeDTO.NoticeResp();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setPublished(notice.getPublished());
        dto.setPublishedAt(notice.getPublishedAt());
        dto.setCreateTime(notice.getCreatedAt());
        return dto;
    }
}