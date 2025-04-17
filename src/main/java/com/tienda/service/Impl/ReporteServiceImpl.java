package com.tienda.service.Impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service 
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private DataSource datasource;
    
    @Override
    public ResponseEntity<Resource> generaReporte(
            String reporte,
            Map<String, Object> parametros,
            String tipo) throws IOException {
            
        try {
            String estilo;
            if("vPdf".equals(tipo)) {
                estilo = "inline; ";
            } else {
                estilo = "attachment";
            }
            
            String reportePath = "reportes";
            
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            
            ClassPathResource fuente = new ClassPathResource(
                    reportePath + File.separator + reporte + ".jasper");
            
            InputStream elReporte = fuente.getInputStream();
            
            JasperPrint reporteJasper = JasperFillManager.fillReport(
                    elReporte,
                    parametros,
                    datasource.getConnection());
                            
            MediaType mediaType = null;
            String archivoSalida = "";
            byte[] data;
            
            if (tipo != null) {
                switch (tipo) {
                    case "Pdf":
                    case "vPdf":
                        JasperExportManager.exportReportToPdfStream(
                                reporteJasper,
                                salida);
                        mediaType = MediaType.APPLICATION_PDF;
                        archivoSalida = reporte + ".pdf";
                        break;
                        
                    case "Xls":
                        JRXlsExporter exportador = new JRXlsExporter();
                        exportador.setExporterInput(
                                new SimpleExporterInput(reporteJasper));
                        exportador.setExporterOutput(
                                new SimpleOutputStreamExporterOutput(salida));
                        SimpleXlsxReportConfiguration configuracion =
                                new SimpleXlsxReportConfiguration();
                        configuracion.setDetectCellType(true);
                        configuracion.setCollapseRowSpan(true);
                        exportador.setConfiguration(configuracion);
                        exportador.exportReport();
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                        archivoSalida = reporte + ".xlsx";
                        break;
                        
                    case "Csv":
                        JRCsvExporter exportadorCsv = new JRCsvExporter();
                        exportadorCsv.setExporterInput(
                                new SimpleExporterInput(reporteJasper));
                        exportadorCsv.setExporterOutput(
                                new SimpleWriterExporterOutput(salida));
                        exportadorCsv.exportReport();
                        mediaType = MediaType.TEXT_PLAIN;
                        archivoSalida = reporte + ".csv";
                        break;
                        
                    default:
                        break;
                }
            }
            
            data = salida.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Disposition",
                    estilo + "filename=\"" + archivoSalida + "\"");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(data.length)
                    .contentType(mediaType)
                    .body(new InputStreamResource(new ByteArrayInputStream(data)));
            
        } catch (SQLException | JRException e) {
            e.printStackTrace();
            return null;
        }
    }
}