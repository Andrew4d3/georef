/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controlador;

/**
 *
 * @author Andrew
 */
public class Modelo {
    
    public class Sitio {
        public int id;
        public int id_usuario;
        public float latitud;
        public float longitud;
        public String fecha;
        public String nombre;
        public String descripcion;
        public String url_imagen;
        public int id_categoria;
        public int sincronizado;
        public int servidor_id;
          
        public Sitio(){
        
        }
    }
    
    public class Categoria {
        public int id;
        public String nombre;
        public String descripcion;
        public int id_usuario;
        public int sincronizado;
        public int servidor_id;
        
        public Categoria(){
        
        }
    }
    
    
    public class Checkpoint {
        public int id;
        public String nombre;
        public float latitud;
        public float longitud;
        public String fecha;
        public String descripcion;
        public String info;
        public String supervisor;
        public String url_imagen;
        public int id_usuario;
        public int sincronizado;
        public int checked_in;
        public int servidor_id;
        
       
        public Checkpoint(){
        
        }
    }
    
    public class Alerta {
        public int id;
        public int id_usuario;
        public String supervisor;
        public String mensaje;
        public String fecha;
        public int visto;
        public int sincronizado;
        public int servidor_id;
                
        public Alerta(){
        
        }
    }
    
    
    public Sitio nuevoSitio(){
        
        Sitio sitio = new Sitio();
        return sitio;
              
    }
    
    public Categoria nuevaCategoria(){
        
        Categoria categoria = new Categoria();
        return categoria;
        
    }
    
    public Checkpoint nuevoCheckpoint(){
        
        Checkpoint checkpoint = new Checkpoint();
        return checkpoint;
         
    }
    
    public Alerta nuevaAlerta(){
        
        Alerta alerta = new Alerta();
        return alerta;
         
    }
    
}
