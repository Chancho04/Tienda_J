

package com.tienda.service;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import java.util.Map;


public interface ReporteService {

    public ResponseEntity<Resource> generaReporte(
    String reporte,
            Map<String, Object> parametros,
            String tipo
            ) 
            throws IOException;
    
}
