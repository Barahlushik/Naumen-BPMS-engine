package ru.naumen.bpms.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.naumen.bpms.controller.dto.ReportInfoResponseDto;
import ru.naumen.bpms.model.report.Report;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "contentReady", expression = "java(report.getStatus() == ru.naumen.bpms.model.report.ReportStatus.COMPLETED)")
    ReportInfoResponseDto toInfoDto(Report report);
}