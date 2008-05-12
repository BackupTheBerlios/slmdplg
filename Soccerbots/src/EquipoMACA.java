import EDU.gatech.cc.is.abstractrobot.ControlSystemSS;
import EDU.gatech.cc.is.util.Vec2;

public class EquipoMACA extends ControlSystemSS {

    private static double zonaPortero = 0.6;

    private static double zonaDefensa = 1.0;

    private static double campo;

    private long tiempo;

    private Vec2 posJugador;

    private Vec2 balon;

    private Vec2 miPorteria;

    private Vec2 porteriaContraria;

    private Vec2[] equipo;

    private Vec2[] oponentes;

    private int num;

    private Vec2 aux2;

    public void Configure() {
    }

    public int TakeStep() {
        //Res.Resultado.Guardar(this, abstract_robot);
        inicializar();
        switch (num) {
        case 0: {
            portero();
            break;
        }
        case 1: {
            defensa();
            break;
        }
        case 2: {
            medio();
            break;
        }
        case 3: {
            delantero();
            break;
        }
        case 4: {
            medioPlacador();
            break;
        }
        default: {
            defensa();
            break;
        }
        }

        return (CSSTAT_OK);
    }

    private void inicializar() {
        num = abstract_robot.getPlayerNumber(abstract_robot.getTime());
        tiempo = abstract_robot.getTime();
        posJugador = abstract_robot.getPosition(tiempo);
        balon = abstract_robot.getBall(tiempo);
        miPorteria = abstract_robot.getOurGoal(tiempo);
        porteriaContraria = abstract_robot.getOpponentsGoal(tiempo);
        equipo = abstract_robot.getTeammates(tiempo);
        oponentes = abstract_robot.getOpponents(tiempo);
        if (miPorteria.x < 0.0) {
            campo = -1;
        } else {
            campo = 1;
        }

    }

    private void portero() {
        double distBalPort = distancia(balon, miPorteria);
        if (distBalPort <= zonaPortero) {
            // Si somos los mas cercanos al balon vamos a por él
            if (cercanoBalon(balon, posJugador, equipo, oponentes)) {
                abstract_robot.setSteerHeading(tiempo, balon.t);
                abstract_robot.setSpeed(tiempo, 1.0);
                // Si el balon esta delante nuestra intentamos disparar
                if (distancia(balon, miPorteria) >= distancia(posJugador,
                        miPorteria)) {
                    if (abstract_robot.canKick(tiempo)) {

                        abstract_robot.kick(tiempo);
                    }
                } else {
                    abstract_robot.setSteerHeading(tiempo, miPorteria.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                }
            } else {
                // Si el balon está en zona de porteria
                if (distancia(balon, miPorteria) <= zonaPortero) {
                    abstract_robot.setSteerHeading(tiempo, balon.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                    if (abstract_robot.canKick(tiempo)) {
                        abstract_robot.kick(tiempo);
                    }
                } else {
                    abstract_robot.setSteerHeading(tiempo, miPorteria.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                    if (abstract_robot.canKick(tiempo)) {
                        abstract_robot.kick(tiempo);
                    }
                }
            }
        } else {
            abstract_robot.setSteerHeading(tiempo, miPorteria.t);
            abstract_robot.setSpeed(tiempo, 1.0);
            if (abstract_robot.canKick(tiempo)) {
                abstract_robot.kick(tiempo);
            }
        }
        if (distBalPort > 0.7) {
            evitarChoque();
        }
    }

    private void defensa() {
        double distBalPort = distancia(balon, miPorteria);
        double distJugPort = distancia(posJugador, miPorteria);
        // Comprobamos si el balón está en nuestra zona vamos a por él
        if (distBalPort <= zonaDefensa) {
            // Si el balon nos ha sobrepasado retrocedemos
            if (distBalPort <= distJugPort) {
                abstract_robot.setSteerHeading(tiempo, balon.t);
                abstract_robot.setSpeed(tiempo, 1.0);
            }
            // Si no, vamos a por él
            else {
                abstract_robot.setSteerHeading(tiempo, balon.t);
                abstract_robot.setSpeed(tiempo, 1.0);
                if (abstract_robot.canKick(tiempo)) {
                    abstract_robot.kick(tiempo);
                }
            }
        }
        // Si estamos fuera de nuestra zona retrocedemos
        else {
            if (((Math.abs(posJugador.x - miPorteria.x) < 0.55))
                    && ((Math.abs(posJugador.y - miPorteria.y) < 0.1))) {
                abstract_robot.setSpeed(tiempo, 0.0);
            } else {
                abstract_robot.setSteerHeading(tiempo, Math.atan2(miPorteria.y,
                        miPorteria.x - 0.5 * campo));
                abstract_robot.setSpeed(tiempo, 1.0);
            }
            if (abstract_robot.canKick(tiempo)) {
                abstract_robot.kick(tiempo);
            }
        }
        if (distBalPort > 0.9) {
            evitarChoque();
        }
    }

    private void medio() {
        double distBalPort = distancia(balon, miPorteria);
        double distBalPortC = distancia(balon, porteriaContraria);
        double distJugPort = distancia(posJugador, miPorteria);
        double distJugPortC = distancia(posJugador, porteriaContraria);
        double distJugBal = distancia(posJugador, balon);
        // Comprobamos si el balon está en nuestro campo
        if (distBalPort <= distBalPortC) {
            // Si el balon nos ha sobrepasado retrocedemos
            Vec2 aux1 = masCercano(balon, oponentes);
            // Cuando la pelota esta muy cercana a la porteria placamos al rival
            // mas cercano a ella
            if (distBalPort < 0.6) {
                abstract_robot.setSteerHeading(tiempo, aux1.t);
                abstract_robot.setSpeed(tiempo, 1.0);
            } else {
                if (distBalPort <= distJugPort) {
                    abstract_robot.setSteerHeading(tiempo, balon.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                }
                // Si no, vamos a por él
                else {
                    abstract_robot.setSteerHeading(tiempo, balon.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                    if (abstract_robot.canKick(tiempo)) {
                        abstract_robot.kick(tiempo);
                    }
                }
            }
        }
        // Si la balon está en el campo contrario
        else {
            // Si la pelota está delante nuestra
            if (distBalPortC < distJugPortC) {
                abstract_robot.setSteerHeading(tiempo, balon.t);
                abstract_robot.setSpeed(tiempo, 1.0);
                if (abstract_robot.canKick(tiempo)) {
                    abstract_robot.kick(tiempo);
                }
            } else {
                abstract_robot.setSteerHeading(tiempo, balon.t);
                abstract_robot.setSpeed(tiempo, 1.0);
            }
        }
        if (distJugBal > 0.8) {
            evitarChoque();
        }
    }

    private void medioPlacador() {
        double distBalPort = distancia(balon, miPorteria);
        double distBalPortC = distancia(balon, porteriaContraria);
        double distJugPort = distancia(posJugador, miPorteria);
        double distJugPortC = distancia(posJugador, porteriaContraria);
        double distJugBal = distancia(posJugador, balon);
        // Variables para controlar el campo en el que está el jugador
        int campoPelota;
        int campoJugador;
        if (distJugPort < distJugPortC) {
            campoJugador = 1;
        } else {
            campoJugador = -1;
        }
        if (distBalPort < distBalPortC) {
            campoPelota = 1;
        } else {
            campoPelota = -1;
        }
        // Comprobamos si el balon está en nuestro campo
        if (distBalPort <= distBalPortC) {
            campoPelota = 1;
            // Si el balon nos ha sobrepasado retrocedemos
            Vec2 aux = masCercano(miPorteria, oponentes);
            // Cuando la pelota esta muy cercana a la porteria placamos al rival
            // mas cercano a ella
            if (distBalPort < 0.9) {
                // Voy a por el rival mas cercano al balón
                abstract_robot.setSteerHeading(tiempo, aux.t);
                abstract_robot.setSpeed(tiempo, 1.0);
            } else {
                // Si el balón nos ha sobrepasado
                if (distBalPort <= distJugPort) {
                    Vec2 aux1 = masCercano(balon, equipo);
                    // Si no hay ningun compañero más cercano a la pelota voy a
                    // por ella
                    if (distancia(balon, aux1) < distancia(balon, posJugador)) {
                        abstract_robot.setSteerHeading(tiempo, balon.t);
                        abstract_robot.setSpeed(tiempo, 1.0);
                    }
                    // Sino voy a la porteria contraria
                    else {
                        abstract_robot.setSteerHeading(tiempo,
                                porteriaContraria.t);
                        abstract_robot.setSpeed(tiempo, 1.0);
                    }
                }
                // Si no, vamos a por él
                else {
                    abstract_robot.setSteerHeading(tiempo, balon.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                    if (abstract_robot.canKick(tiempo)) {
                        abstract_robot.kick(tiempo);
                    }
                }
            }
        }
        // Si el balon está en el campo contrario
        else {
            campoPelota = -1;
            // Si la pelota está delante nuestra la busco para tirar
            if (distBalPortC < distJugPortC) {
                Vec2 aux = masCercano(balon, equipo);
                // Si hay ningun compañero más cercano a la pelota voy a por
                // ella
                if (distancia(balon, aux) < distancia(balon, posJugador)) {
                    abstract_robot.setSteerHeading(tiempo, porteriaContraria.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                    if (abstract_robot.canKick(tiempo)) {
                        abstract_robot.kick(tiempo);
                    }
                }
                // Sino voy a la porteria contraria
                else {
                    abstract_robot.setSteerHeading(tiempo, balon.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                    if (abstract_robot.canKick(tiempo)) {
                        abstract_robot.kick(tiempo);
                    }
                }
            } else {
                // Si no trato de placar al rival mas cercano a ella
                Vec2 aux = masCercano(balon, oponentes);
                abstract_robot.setSteerHeading(tiempo, aux.t);
                abstract_robot.setSpeed(tiempo, 1.0);
            }
            if (distJugBal > 0.7) {
                evitarChoque();
            }
        }
        // Si el jugador y la pelota están en campos distintos evito el bloqueo
        if (campoJugador != campoPelota) {
            evitarChoque();
        }
    }

    private void delantero() {
        double distBalPort = distancia(balon, miPorteria);
        double distBalPortC = distancia(balon, porteriaContraria);
        double distJugBal = distancia(posJugador, balon);
        // Comprobamos si el balon está en nuestro campo
        if (distBalPort <= distBalPortC) {
            // Vamos a por el rival más cercano a su porteria
            Vec2 aux2 = masCercano(porteriaContraria, equipo);
            // Si somos el jugador de nuestro equipo más cercano a la pelota,
            // vamos a por ella
            if (distancia(aux2, balon) > distJugBal) {
                abstract_robot.setSteerHeading(tiempo, balon.t);
                abstract_robot.setSpeed(tiempo, 1.0);
            }
            // Si no, vamos a por el rival más cercano a su porteria
            else {
                Vec2 aux3 = masCercano(porteriaContraria, oponentes);
                Vec2 aux1 = masCercano(porteriaContraria, oponentes);
                abstract_robot.setSteerHeading(tiempo, aux1.t);
                abstract_robot.setSpeed(tiempo, 1.0);
                // Si el rival está más lejos de la porteria contraria que yo
                // evito el bloqueo
                if (distancia(aux3, porteriaContraria) > distancia(posJugador,
                        porteriaContraria)) {
                    evitarChoque();
                }
            }
        }
        // Si el balon está en el campo contrario
        else {
            Vec2 aux = masCercano(balon, equipo);
            // Si somos el jugador más cercano a la pelota vamos a por ella
            if ((distancia(aux, balon) > distJugBal)) {
                abstract_robot.setSteerHeading(tiempo, balon.t);
                abstract_robot.setSpeed(tiempo, 1.0);
                if (abstract_robot.canKick(tiempo)) {
                    abstract_robot.kick(tiempo);
                }
            }
            // Si no somos el jugador más cercano a la pelota
            else {
                // Si la pelota está muy cerca de la linea de fondo
                if ((Math.abs(porteriaContraria.y - balon.y) <= 0.6)) {
                    // Nos colocamos enfrente de la porteria esperando el balón
                    abstract_robot.setSteerHeading(tiempo, Math.atan2(
                            porteriaContraria.y, porteriaContraria.x + 0.5
                                    * campo));
                    abstract_robot.setSpeed(tiempo, 1.0);
                    if (abstract_robot.canKick(tiempo)) {
                        abstract_robot.kick(tiempo);
                    }
                } else {
                    // En otro caso vamos a por el rival más cercano a su
                    // porteria
                    Vec2 aux2 = masCercano(porteriaContraria, oponentes);
                    abstract_robot.setSteerHeading(tiempo, aux2.t);
                    abstract_robot.setSpeed(tiempo, 1.0);
                    if (abstract_robot.canKick(tiempo)) {
                        abstract_robot.kick(tiempo);
                    }
                }
            }
        }
    }

    private boolean cercanoBalon(Vec2 balon, Vec2 jugador, Vec2[] companeros,
            Vec2[] rivales) {
        double distMinima = distancia(balon, jugador);
        int i = 0;
        double distAux = Double.MAX_VALUE;
        double val = 0;
        for (i = 0; i < companeros.length; i++) {
            val = distancia(balon, companeros[i]);
            if (val < distAux)
                distAux = val;
        }
        for (i = 0; i < rivales.length; i++) {
            val = distancia(balon, rivales[i]);
            if (val < distAux)
                distAux = val;
        }

        return (distAux >= distMinima);
    }

    private Vec2 masCercano(Vec2 destino, Vec2[] jugadores) {
        double distMinima = Double.MAX_VALUE;
        Vec2 result = new Vec2(0, 0);
        Vec2 aux = new Vec2(0, 0);

        for (int i = 0; i < jugadores.length; i++) {
            aux.sett(jugadores[i].t);
            aux.setr(jugadores[i].r);
            aux.sub(destino);
            if (aux.r < distMinima) {
                result = jugadores[i];
                distMinima = aux.r;
            }
        }

        return result;
    }

    private double distancia(Vec2 vec1, Vec2 vec2) {
        Vec2 aux = new Vec2(0, 0);
        aux.sett(vec1.t);
        aux.setr(vec1.r);
        aux.sub(vec2);
        return aux.r;
    }

    private void evitarChoque() {
        Vec2 aux1 = masCercano(posJugador, equipo);
        aux2 = masCercano(posJugador, oponentes);
        if (aux1.r < abstract_robot.RADIUS * 1.4) {
            posJugador.setx(-aux1.x);
            posJugador.sety(-aux1.y);
            posJugador.setr(1.0);
            abstract_robot.setSteerHeading(tiempo, posJugador.t);
            abstract_robot.setSpeed(tiempo, posJugador.r);
        }

        else {
            if (aux2.r < abstract_robot.RADIUS * 1.4) {
                posJugador.setx(-aux2.x);
                posJugador.sety(-aux2.y);
                posJugador.setr(1.0);
                abstract_robot.setSteerHeading(tiempo, posJugador.t);
                abstract_robot.setSpeed(tiempo, posJugador.r);
            }
        }

    }
}
