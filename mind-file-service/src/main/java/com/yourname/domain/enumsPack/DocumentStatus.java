package com.yourname.domain.enumsPack;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 文档状态枚举
 */
@Getter
public enum DocumentStatus {
    PENDING(0, "等待上传", "创建记录但文件未上传"),
    
    UPLOADED(1, "已上传", "文件已上传，等待解析"),
    
    PARSING(2, "解析中", "正在使用Apache Tika解析文档内容"),
    
    COMPLETED(3, "解析完成", "文档解析完成，内容可用"),
    
    FAILED(4, "解析失败", "文档解析失败");

    @EnumValue
    private final Integer code;
    private final String description;
    private final String detail;

    DocumentStatus(Integer code, String description, String detail) {
        this.code = code;
        this.description = description;
        this.detail = detail;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getDetail() {
        return detail;
    }

    public static DocumentStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DocumentStatus status : DocumentStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 检查状态是否可进行解析
     */
    public static boolean canParse(Integer code) {
        DocumentStatus status = getByCode(code);
        return status != null && (status == UPLOADED || status == FAILED);
    }

    /**
     * 检查状态是否为最终状态
     */
    public static boolean isFinal(Integer code) {
        DocumentStatus status = getByCode(code);
        return status != null && (status == COMPLETED || status == FAILED);
    }

    /**
     * 检查状态是否正在处理中
     */
    public static boolean isProcessing(Integer code) {
        DocumentStatus status = getByCode(code);
        return status != null && status == PARSING;
    }

    /**
     * 获取所有可用的状态码
     */
    public static Integer[] getAvailableCodes() {
        DocumentStatus[] values = values();
        Integer[] codes = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].getCode();
        }
        return codes;
    }

    @Override
    public String toString() {
        return this.description + "(" + this.code + ")";
    }
}