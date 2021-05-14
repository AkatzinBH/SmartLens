package com.example.smartlens;

public class Notificacion
{
    private String mensaje;
    private String paquete;
    private String remitente;
    private String fechahora;


    public Notificacion(String mensaje, String remitente, String paquete, String fechahora)
    {
        this.mensaje = mensaje;
        this.remitente = remitente;
        this.paquete = paquete;
        this.fechahora = fechahora;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getPaquete() {
        return paquete;
    }

    public void setPaquete(String paquete) {
        this.paquete = paquete;
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }

    public String getFechahora() {
        return fechahora;
    }

    public void setFechahora(String fechahora) {
        this.fechahora = fechahora;
    }
}
