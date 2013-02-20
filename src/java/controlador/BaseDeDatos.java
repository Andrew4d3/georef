/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controlador;

import com.google.gson.Gson;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;



/**
 *
 * @author Andrew
 */
public class BaseDeDatos {

    
    
    public static String url = "jdbc:postgresql://localhost/georef";
    public static String user = "postgres";
    public static String password = "admin";
    
    
    private class Geometria{
        public String type;
        public float[] coordinates;
        
    
        public Geometria(){
            
        }
    
    }
    
    private class respuestaPrimera{
        public String resultado="ok";
        public Modelo.Categoria[] categorias_existentes;
        public Modelo.Categoria[] categorias_creadas;
        public Modelo.Sitio[] sitios_existentes;
        public Modelo.Sitio[] sitios_creados;
        public Modelo.Checkpoint[] checkpoints_existentes;
        public Modelo.Alerta[] alertas_existentes;
        
    
        public respuestaPrimera(){
            
        }
    
    }
    
    private class respuestaSegunda{
        public String resultado="ok";
        public Modelo.Categoria[] categorias;
        public Modelo.Sitio[] sitios;
        public Modelo.Alerta[] alertas;
        public Modelo.Checkpoint[] checkpoints;
        public Modelo.Alerta[] alertas_nuevas;
        public Modelo.Checkpoint[] checkpoints_nuevos;
        
    
        public respuestaSegunda(){
            
        }
    
    }
    
    
    
    
    public class resultadoAutenticar {
        public String resultado;
        
        //constructor
        public resultadoAutenticar(){
        
        }
    }
    
   private Connection conectar() throws SQLException, ClassNotFoundException{
        
     
       Connection con = null;
        
       Class.forName("org.postgresql.Driver");
       con = DriverManager.getConnection(url, user, password);
        
       return con;
   }
   
   private int getUsuarioId(String usuario) throws ClassNotFoundException{
        
       int usuario_id=0;
       try {
            Connection con = conectar();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM usuarios WHERE usuario='"+usuario+"'");

            rs.next();

            usuario_id = rs.getInt("id");

            con.close();
           
        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return usuario_id;
   }
   
   private String getAdministrador(int administrador_id) throws ClassNotFoundException{
        
       String administrador="";
       try {
            Connection con = conectar();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM administradores WHERE id="+administrador_id);

            rs.next();

            administrador = rs.getString("administrador");

            con.close();
           
        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        return administrador;
   }
   
   
   private int get_categoriaid_serv(int id_cliente, Modelo.Categoria[] categorias){
       
       int servidor_id = 0;
       
       for(int i=0;i<categorias.length;i++){
           
           if(categorias[i].id==id_cliente){
               
               servidor_id = categorias[i].servidor_id;
               
               
           }
       
       }
            
       return servidor_id;
   }   

    
    
    public String autenticar(String usuario, String pass) throws SQLException, ClassNotFoundException{
        
        
        Connection con = conectar();
        String result="error";
               
        
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM usuarios WHERE usuario='"+usuario+"'");
            
            //System.out.println(rs.getString(1));
            
            if(!rs.next()){
                
                result="inexistente";
            }
            else{
                if(rs.getString("pass").equals(pass)){
                    
                    result="autenticado";
                }
                else{
                    result="passincorrecto";
                }
                
            }
          
            con.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
   
        
        
        return result;
        
    }
    
    public String error_json(String r){
        
        resultadoAutenticar result = new resultadoAutenticar();
        
        result.resultado=r;
        
        Gson gson = new Gson();
        String json_r = gson.toJson(result);
        
        
        return json_r;
    }
    
    public Modelo.Categoria[] extraer_categorias(String usuario) throws ClassNotFoundException{
                    
        Modelo.Categoria[] categorias_existentes=null;
        try {
            
            int usuario_id = getUsuarioId(usuario);
            boolean hay_categorias=false;
            
            Connection con = conectar();
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = st.executeQuery("SELECT * FROM categorias WHERE id_usuario="+usuario_id);

            int n_rows=0;
            if(rs.next()){
                hay_categorias=true;
                rs.last();
                n_rows = rs.getRow(); 
            }
            rs.beforeFirst();
            
            
            if(hay_categorias){
                
                categorias_existentes =new Modelo.Categoria[n_rows];
                                
                int i=0;
                while(rs.next()){
                    
                    categorias_existentes[i] = new Modelo().nuevaCategoria();
                    
                    categorias_existentes[i].id_usuario = usuario_id;                                                                         
                    categorias_existentes[i].nombre = rs.getString("nombre");
                    categorias_existentes[i].descripcion = rs.getString("descripcion");
                    categorias_existentes[i].sincronizado = 1;
                    categorias_existentes[i].servidor_id = rs.getInt("id");

                    i++;
                    
                }
            }
            else{
                categorias_existentes = new Modelo.Categoria[0];
            }
        
             
            con.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return categorias_existentes;
    }    

    public Modelo.Categoria[] actualizarTablaCategorias(String json_categorias,String usuario) throws  ClassNotFoundException, ParseException{
        
        Gson gson = new Gson();
        Modelo.Categoria[] categorias= gson.fromJson(json_categorias, Modelo.Categoria[].class);
        
        try {  
            
            int usuario_id = getUsuarioId(usuario);
            
            Connection con = conectar();
       
            for(int i=0;i<categorias.length;i++){
                
                if(categorias[i].sincronizado==0 && categorias[i].servidor_id==0){
                            
                    String query = "INSERT INTO categorias(nombre,descripcion,id_usuario,sincronizado) VALUES(?,?,?,?)";

                    PreparedStatement pst = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

                    pst.setString(1, categorias[i].nombre);
                    pst.setString(2, categorias[i].descripcion);
                    pst.setInt(3, usuario_id);
                    pst.setBoolean(4, true);

                    categorias[i].sincronizado=1;
                    
                    pst.executeUpdate();

                    ResultSet rs = pst.getGeneratedKeys();

                    if(rs.next()){
                        categorias[i].servidor_id = rs.getInt("id");
                        System.out.println("Se ha insertado la categoria: "+categorias[i].servidor_id);
                    }
    
                }
                else if(categorias[i].sincronizado==0 && categorias[i].servidor_id!=0){
                    
                    String queryActualizar = "UPDATE categorias SET nombre=?, descripcion=?  WHERE id=? ";
                    PreparedStatement pst = con.prepareStatement(queryActualizar);
                    pst.setString(1, categorias[i].nombre);
                    pst.setString(2, categorias[i].descripcion);
                    pst.setInt(3, categorias[i].servidor_id);
                    pst.executeUpdate();
                    categorias[i].sincronizado=1;
                    System.out.println("Se ha actualizado la categoria: "+categorias[i].servidor_id);
                
                    
                    
                }
            }
       

        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }

        return categorias;
   
    }
    
    
    public Modelo.Sitio[] extraer_sitios(String usuario) throws ClassNotFoundException{
                    
        Modelo.Sitio[] sitios_existentes=null;
        try {
            
            int usuario_id = getUsuarioId(usuario);
            boolean hay_sitios=false;
            
            Connection con = conectar();
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = st.executeQuery("SELECT *,ST_AsGeoJSON(coordenadas) AS geo_coord FROM sitios WHERE id_usuario="+usuario_id);

            int n_rows=0;
            if(rs.next()){
                hay_sitios=true;
                rs.last();
                n_rows = rs.getRow(); 
            }
            rs.beforeFirst();
            
            
            if(hay_sitios){
                
                sitios_existentes =new Modelo.Sitio[n_rows];
                Gson gson = new Gson();
                Geometria geom = new Geometria();
                
                int i=0;
                while(rs.next()){
                    
                    sitios_existentes[i] = new Modelo().nuevoSitio();
                    
                    sitios_existentes[i].id_usuario = usuario_id;
                  
                    geom = gson.fromJson(rs.getString("geo_coord"), Geometria.class);
                    
                    sitios_existentes[i].longitud = geom.coordinates[0];
                    sitios_existentes[i].latitud = geom.coordinates[1];
                    
                    java.util.Date newDate = rs.getTimestamp("fecha");
                                        
                    sitios_existentes[i].fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(newDate);
                    
                    sitios_existentes[i].nombre = rs.getString("nombre");
                    sitios_existentes[i].id_categoria = rs.getInt("id_categoria");
                    sitios_existentes[i].url_imagen = "";
                    sitios_existentes[i].descripcion = rs.getString("descripcion");
                    sitios_existentes[i].sincronizado = 1;
                    sitios_existentes[i].servidor_id = rs.getInt("id");
                    
                    
                    //System.out.println("La longitud es: "+sitios_existentes[i].longitud+" y la latitud es: "+sitios_existentes[i].latitud);
                    
                    
                    i++;
                    
                }
            }
            else{
                sitios_existentes = new Modelo.Sitio[0];
            }
        
             
            con.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return sitios_existentes;
    }
    
    public Modelo.Sitio[] actualizarTablaSitios(String json_sitios,String usuario, Modelo.Categoria[] categorias) throws  ClassNotFoundException, ParseException{
        
        Gson gson = new Gson();
        Modelo.Sitio[] sitios= gson.fromJson(json_sitios, Modelo.Sitio[].class);
        
        try {  
            
            int usuario_id = getUsuarioId(usuario);
            
            Connection con = conectar();
       
            for(int i=0;i<sitios.length;i++){
               
                if(sitios[i].sincronizado==0 && sitios[i].servidor_id==0){

                    String query = "INSERT INTO sitios(nombre,fecha,descripcion,id_usuario,id_categoria,sincronizado,coordenadas) VALUES(?,?,?,?,?,?,ST_GeomFromText(?, 4326))";

                    PreparedStatement pst = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);

                    pst.setString(1, sitios[i].nombre);

                    Date fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(sitios[i].fecha);

                    pst.setTimestamp(2, new java.sql.Timestamp(fecha.getTime()));

                    pst.setString(3, sitios[i].descripcion);
                    pst.setInt(4, usuario_id);
                    if(sitios[i].id_categoria!=0){
                        pst.setInt(5, get_categoriaid_serv(sitios[i].id_categoria, categorias));
                    }
                    else{
                        pst.setNull(5, java.sql.Types.INTEGER);
                    }
                    pst.setBoolean(6, true);
                    sitios[i].sincronizado=1;

                    String coordenadas = "POINT("+sitios[i].longitud+" "+sitios[i].latitud+")";

                    pst.setString(7, coordenadas);

                    pst.executeUpdate();

                    ResultSet rs = pst.getGeneratedKeys();

                    if(rs.next()){
                        sitios[i].servidor_id = rs.getInt("id");
                        System.out.println("Se ha insertado el sitio: "+sitios[i].servidor_id);
                    }
                }
                else if(sitios[i].sincronizado==0 && sitios[i].servidor_id!=0){
                    
                    String queryActualizar = "UPDATE sitios SET nombre=?, descripcion=?, id_categoria=?  WHERE id=? ";
                    PreparedStatement pst = con.prepareStatement(queryActualizar);
                    pst.setString(1, sitios[i].nombre);
                    pst.setString(2, sitios[i].descripcion);
                    if(sitios[i].id_categoria!=0){
                       pst.setInt(3, get_categoriaid_serv(sitios[i].id_categoria, categorias)); 
                    }
                    else{
                       pst.setNull(3, java.sql.Types.INTEGER);
                    }
                    
                    pst.setInt(4, sitios[i].servidor_id);
                    pst.executeUpdate();
                    System.out.println("Sitio "+sitios[i].servidor_id+" ha sido actualizado");
                    sitios[i].sincronizado=1;
                    
                    
                }

            }
       

        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sitios;
   
    }
    
       public Modelo.Alerta[] actualizarTablaAlertas(String json_alertas) throws  ClassNotFoundException, ParseException{
        
        Gson gson = new Gson();
        Modelo.Alerta[] alertas= gson.fromJson(json_alertas, Modelo.Alerta[].class);
        
        try {  
        
            Connection con = conectar();
       
            for(int i=0;i<alertas.length;i++){
                
                
                if(alertas[i].sincronizado==0 ){
                    
                    String queryActualizar = "UPDATE alertas SET visto=? WHERE id=? ";
                    PreparedStatement pst = con.prepareStatement(queryActualizar);
                  
                    pst.setBoolean(1, true);
                    pst.setInt(2, alertas[i].servidor_id);
                    
                    pst.executeUpdate();
                    alertas[i].sincronizado=1;
                    System.out.println("Se ha actualizado la alerta "+alertas[i].servidor_id);
               
                    
                }
            }
       

        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }

        return alertas;
   
    }
       
    public Modelo.Checkpoint[] actualizarTablaCheckpoints(String json_checkpoints) throws  ClassNotFoundException, ParseException{
        
        Gson gson = new Gson();
        Modelo.Checkpoint[] checkpoints= gson.fromJson(json_checkpoints, Modelo.Checkpoint[].class);
        
        try {  
            
            
            Connection con = conectar();
       
            for(int i=0;i<checkpoints.length;i++){
             
                if(checkpoints[i].sincronizado==0 ){
                    
                    String queryActualizar = "UPDATE checkpoints SET checked_in=? WHERE id=? ";
                    PreparedStatement pst = con.prepareStatement(queryActualizar);
                  
                    pst.setBoolean(1, true);
                    pst.setInt(2, checkpoints[i].servidor_id);
                    
                    pst.executeUpdate();
                    checkpoints[i].sincronizado=1;
                    System.out.println("Se ha actualizado el checkpoint "+checkpoints[i].servidor_id);
               
                    
                }
            }
       

        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }

        return checkpoints;
   
    }
 

    public Modelo.Alerta[] extraer_alertas(String usuario, Boolean primera_sincronizacion) throws ClassNotFoundException{
                    
        Modelo.Alerta[] alertas_existentes=null;
        try {
            
            int usuario_id = getUsuarioId(usuario);
            boolean hay_alertas=false;
            
            Connection con = conectar();
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            
            String query;
            if(primera_sincronizacion){
                query = "SELECT * FROM alertas WHERE visto=FALSE AND id_usuario="+usuario_id;
            }
            else{
                query = "SELECT * FROM alertas WHERE visto=FALSE AND sincronizado=FALSE AND id_usuario="+usuario_id;
            }
            
            ResultSet rs = st.executeQuery(query);

            int n_rows=0;
            if(rs.next()){
                hay_alertas=true;
                rs.last();
                n_rows = rs.getRow(); 
            }
            rs.beforeFirst();
            
            
            if(hay_alertas){
                
                alertas_existentes =new Modelo.Alerta[n_rows];
                                
                int i=0;
                while(rs.next()){
                    
                    alertas_existentes[i] = new Modelo().nuevaAlerta();
                    
                    alertas_existentes[i].id_usuario = usuario_id;                                                                         
                    alertas_existentes[i].mensaje = rs.getString("mensaje");
                    alertas_existentes[i].supervisor = getAdministrador(rs.getInt("id_administrador")); 
                    
                    java.util.Date newDate = rs.getDate("fecha");
                                        
                    alertas_existentes[i].fecha = new SimpleDateFormat("dd/MM/yyyy").format(newDate);
                    
                    
                    alertas_existentes[i].sincronizado = 1;
                    alertas_existentes[i].visto = 0;
                    alertas_existentes[i].servidor_id = rs.getInt("id");

                    i++;
                    
                }
                
                String queryActualizar = "UPDATE alertas SET sincronizado=? WHERE id_usuario=? ";
                PreparedStatement pst = con.prepareStatement(queryActualizar);
                pst.setBoolean(1, true);
                pst.setInt(2, usuario_id);
                pst.executeUpdate();
                
                
            }
            else{
                alertas_existentes = new Modelo.Alerta[0];
            }
        
             
            con.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return alertas_existentes;
    }

    public Modelo.Checkpoint[] extraer_checkpoints(String usuario, Boolean primera_sincronizacion) throws ClassNotFoundException{
                    
        Modelo.Checkpoint[] checkpoints_existentes=null;
        try {
            
            int usuario_id = getUsuarioId(usuario);
            boolean hay_checkpoints=false;
            
            Connection con = conectar();
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            
            String query;
            if(primera_sincronizacion){
                query = "SELECT *,ST_AsGeoJSON(coordenadas) AS geo_coord FROM checkpoints WHERE id_usuario="+usuario_id;
            }
            else{
                query = "SELECT *,ST_AsGeoJSON(coordenadas) AS geo_coord FROM checkpoints WHERE sincronizado=FALSE and id_usuario="+usuario_id;
            }
            
            ResultSet rs = st.executeQuery(query);

            int n_rows=0;
            if(rs.next()){
                hay_checkpoints=true;
                rs.last();
                n_rows = rs.getRow(); 
            }
            rs.beforeFirst();
            
            
            if(hay_checkpoints){
                
                checkpoints_existentes =new Modelo.Checkpoint[n_rows];
                Gson gson = new Gson();
                Geometria geom = new Geometria();
                                
                int i=0;
                while(rs.next()){
                    
                    checkpoints_existentes[i] = new Modelo().nuevoCheckpoint();
                    
                    checkpoints_existentes[i].nombre = rs.getString("nombre");
                    
                    geom = gson.fromJson(rs.getString("geo_coord"), Geometria.class);
                    
                    checkpoints_existentes[i].longitud = geom.coordinates[0];
                    checkpoints_existentes[i].latitud = geom.coordinates[1];
                    
                    java.util.Date newDate = rs.getTimestamp("fecha");                   
                    checkpoints_existentes[i].fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(newDate);
                    
                    checkpoints_existentes[i].descripcion = rs.getString("descripcion");
                    checkpoints_existentes[i].info = rs.getString("info");
                    checkpoints_existentes[i].supervisor = getAdministrador(rs.getInt("id_administrador"));
                    checkpoints_existentes[i].url_imagen =""; 
                    checkpoints_existentes[i].id_usuario = usuario_id;   
                    checkpoints_existentes[i].sincronizado = 1;
                    
                    if(rs.getBoolean("checked_in")){
                        checkpoints_existentes[i].checked_in = 1;
                    }
                    else{
                        checkpoints_existentes[i].checked_in = 0;
                    }
                    
                    
                    checkpoints_existentes[i].servidor_id = rs.getInt("id");

                    i++;
                    
                }
                
                String queryActualizar = "UPDATE checkpoints SET sincronizado=? WHERE id_usuario=? ";
                PreparedStatement pst = con.prepareStatement(queryActualizar);
                pst.setBoolean(1, true);
                pst.setInt(2, usuario_id);
                pst.executeUpdate();
                
                
                          
            }
            else{
                checkpoints_existentes = new Modelo.Checkpoint[0];
            }
        
             
            con.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return checkpoints_existentes;
    }
    
    public void eliminar_sitios(String json_sitborrados) throws ClassNotFoundException{
        
        Gson gson = new Gson();
        int[] sitborrados= gson.fromJson(json_sitborrados, int[].class);
        
        try {  
        
            Connection con = conectar();
       
            for(int i=0;i<sitborrados.length;i++){
           
                String queryBorrar = "DELETE FROM sitios WHERE id=? ";
                PreparedStatement pst = con.prepareStatement(queryBorrar);
  
                pst.setInt(1, sitborrados[i]);
                
                pst.executeUpdate();
                System.out.println("Sitio "+sitborrados[i]+" borrado");
            }
       

        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
    }
    
    public void eliminar_categorias(String json_catborradas) throws ClassNotFoundException{
        
        Gson gson = new Gson();
        int[] catborradas= gson.fromJson(json_catborradas, int[].class);
        
        try {  
        
            Connection con = conectar();
       
            for(int i=0;i<catborradas.length;i++){
           
                String queryBorrar = "DELETE FROM categorias WHERE id=? ";
                PreparedStatement pst = con.prepareStatement(queryBorrar);
  
                pst.setInt(1, catborradas[i]);

                pst.executeUpdate();
                System.out.println("Categoria "+catborradas[i]+" borrada");
            
            }
       

        } catch (SQLException ex) {
            Logger.getLogger(BaseDeDatos.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
    }
    
    public String construirJson1ra(Modelo.Categoria[] categorias_existentes, Modelo.Categoria[] categorias_creadas,  Modelo.Sitio[] sitios_existentes, Modelo.Sitio[] sitios_creados, Modelo.Alerta[] alertas_existentes, Modelo.Checkpoint[] checkpoints_existentes){
        
        String json_respuesta="";
        Gson gson = new Gson();
        respuestaPrimera respuesta = new respuestaPrimera();
        
        respuesta.categorias_existentes=categorias_existentes;
        respuesta.categorias_creadas=categorias_creadas;
        respuesta.sitios_existentes=sitios_existentes;
        respuesta.sitios_creados=sitios_creados;
        respuesta.alertas_existentes=alertas_existentes;
        respuesta.checkpoints_existentes=checkpoints_existentes;
        
        json_respuesta = gson.toJson(respuesta);
        
        return json_respuesta;
    }
    
    public String construirJson2da(Modelo.Categoria[] categorias, Modelo.Sitio[] sitios, Modelo.Alerta[] alertas, Modelo.Checkpoint[] checkpoints, Modelo.Alerta[] alertas_nuevas, Modelo.Checkpoint[] checkpoints_nuevos){
        
        String json_respuesta="";
        Gson gson = new Gson();
        respuestaSegunda respuesta = new respuestaSegunda();
        
        respuesta.categorias=categorias;
        respuesta.sitios=sitios;
        respuesta.alertas=alertas;
        respuesta.checkpoints=checkpoints;
        respuesta.alertas_nuevas=alertas_nuevas;
        respuesta.checkpoints_nuevos=checkpoints_nuevos;
        
        
        json_respuesta = gson.toJson(respuesta);
        
        return json_respuesta;
    }
    
    
}

