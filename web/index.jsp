<%-- 
    Document   : index
    Created on : 05/02/2013, 10:15:04 PM
    Author     : Andrew
--%>


<%@ page import="controlador.*" %>

<%

    if(request.getParameter("funcion")==null){
        System.out.println("No hay peticiones... Cerrando");
        return;
    }
    
    
    String usuario,pass,json_sitios,json_categorias,json_alertas,json_checkpoints,r,json_error,json_catborradas,json_sitborrados;
    BaseDeDatos bd;

    
    if (((String) request.getParameter("funcion")).equals("primera_sincronizacion")){
        
        usuario = (String) request.getParameter("usuario");
        pass = (String) request.getParameter("pass");
        json_sitios = (String) request.getParameter("sitios");
        json_categorias = (String) request.getParameter("categorias");
        System.out.println("Se ha recibido datos del usuario "+usuario);
        bd = new BaseDeDatos();
        
        r = bd.autenticar(usuario, pass);
        System.out.println("Resultado de la autenticacion: "+r);
        
        if(r.equals("inexistente") || r.equals("passincorrecto")){
            json_error = bd.error_json(r);
            System.out.println("Contraseña o Usuario no coinciden");
            out.print(json_error);
        }
        else{
            
            Modelo.Categoria[] categorias_existentes, categorias_creadas;
            Modelo.Sitio[] sitios_existentes, sitios_creados;
            Modelo.Checkpoint[] checkpoints_existentes;
            Modelo.Alerta[] alertas_existentes;
            
            
            categorias_existentes = bd.extraer_categorias(usuario);
            System.out.println("Nro de categorias ya existentes\n"+categorias_existentes.length);
            System.out.println("Se procedera agregar estas categorias\n"+json_categorias);
            categorias_creadas = bd.actualizarTablaCategorias(json_categorias, usuario);
            
            
            sitios_existentes = bd.extraer_sitios(usuario);
            System.out.println("Nro de sitios ya existentes\n"+sitios_existentes.length);
            System.out.println("Se procedera agregar estos sitios\n"+json_sitios);
            sitios_creados = bd.actualizarTablaSitios(json_sitios, usuario, categorias_creadas);
            
            checkpoints_existentes = bd.extraer_checkpoints(usuario, true);
            System.out.println("Nro de checkpoints ya existentes\n"+checkpoints_existentes.length);

            alertas_existentes = bd.extraer_alertas(usuario, true);
            System.out.println("Nro de alertas ya existentes\n"+alertas_existentes.length);   
            
            
            String json_respuesta=bd.construirJson1ra(categorias_existentes, categorias_creadas, sitios_existentes, sitios_creados, alertas_existentes, checkpoints_existentes);
            
            System.out.println(json_respuesta);
            
            out.print(json_respuesta);
            
        }
        
    
    }
    else if (((String) request.getParameter("funcion")).equals("segunda_sincronizacion")){
        
        usuario = (String) request.getParameter("usuario");
        pass = (String) request.getParameter("pass");
        json_sitios = (String) request.getParameter("sitios");
        json_categorias = (String) request.getParameter("categorias");
        json_checkpoints = (String) request.getParameter("checkpoints");
        json_alertas = (String) request.getParameter("alertas");
        
        json_sitborrados = (String) request.getParameter("sitborrados");
        json_catborradas = (String) request.getParameter("catborradas");
        System.out.println("Se ha recibido datos del usuario "+usuario);
        bd = new BaseDeDatos();
        
        r = bd.autenticar(usuario, pass);
        System.out.println("Resultado de la autenticacion: "+r);
        
        if(r.equals("inexistente") || r.equals("passincorrecto")){
            json_error = bd.error_json(r);
            System.out.println("Contraseña o Usuario no coinciden");
            out.print(json_error);
        }
        else{
            System.out.println("Se reciben los siguientes datos");
            System.out.println("Sitios: "+json_sitios);
            System.out.println("Categorias: "+json_categorias);
            System.out.println("Checkpoints: "+json_checkpoints);
            System.out.println("Alertas: "+json_alertas);
            System.out.println("Sitios Borrados: "+json_sitborrados);
            System.out.println("Categorias Borradas: "+json_catborradas);
            
            
            
            Modelo.Categoria[] categorias = bd.actualizarTablaCategorias(json_categorias, usuario);
            Modelo.Sitio[] sitios = bd.actualizarTablaSitios(json_sitios, usuario,categorias);
            
            
            
            
            if(!json_sitborrados.equals("null")){
                
                bd.eliminar_sitios(json_sitborrados);
            }
            
            if(!json_catborradas.equals("null")){
                bd.eliminar_categorias(json_catborradas);
            }
            
            Modelo.Alerta[] alertas = bd.actualizarTablaAlertas(json_alertas);
            Modelo.Checkpoint[] checkpoints = bd.actualizarTablaCheckpoints(json_checkpoints);
            
            Modelo.Alerta[] alertas_nuevas = bd.extraer_alertas(usuario, false);
            Modelo.Checkpoint[] checkpoints_nuevos = bd.extraer_checkpoints(usuario, false);
  
            
            String json_respuesta = bd.construirJson2da(categorias, sitios, alertas, checkpoints, alertas_nuevas, checkpoints_nuevos);
            
            System.out.println("Se enviara la siguiente respuesta\n"+json_respuesta);
            
            out.print(json_respuesta);
        }
        
    
    }
    
    
    
    
    
%>
