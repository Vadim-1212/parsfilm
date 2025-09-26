package com.parsfilm.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

// модель отчета в виде класса для xml
@JacksonXmlRootElement(localName = "report")
public class ReportDto {
    @JacksonXmlElementWrapper(localName = "pages")
    @JacksonXmlProperty(localName = "page")
    List<PageDto> pages;

    public ReportDto(List<PageDto> pages) {
        this.pages = pages;
    }

    public ReportDto() {

    }

    public List<PageDto> getPages() {
        return pages;
    }

    public void setPages(List<PageDto> pages) {
        this.pages = pages;
    }
}
