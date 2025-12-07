package com.yourname.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsDocumentVO {

    private Long id;
    // 【关键】这个title应该是已被<em>标签包裹的字符串
    private String title;
    // 内容通常很长，一般只返回高亮片段，而不是全部原文
    private String highlightContentSnippet;

    private String author;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
