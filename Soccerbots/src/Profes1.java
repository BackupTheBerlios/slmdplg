import java.util.Vector;
import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.abstractrobot.*;

/**
 *
 * <p>Title: SoccerBots </p>
 * MODIFICADO: Este es el equipo MamboFC pero lo cambiamos para pasárselo a
 * los alumnos de ISBC de 2007
 *
 * <p>Description: Clase que implementa una estrategia de un equipo de
 * SoccerBots heterogeneo, de asignación de roles y tácticas dinámica y
 * con historial de posiciones del balón </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * @author Francisco Calderón Setién
 * - José María Vivas Rebuelta
 * - César Andrés Sánchez
 *
 * @version 1.0
 */
public class Profes1
    extends ControlSystemSS {
    

  // Constantes para designar los valores de pi y 2*pi
  protected static final double PI = Math.PI;
  protected static final double PI2 = Math.PI * 2;

  // Constantes para desginar los distintos tipos de jugadores
  static final int BLOQUEADOR = 0;
  static final int DELANTERO = 1;
  static final int CENTRAL = 2;
  static final int DEFENSA_PRESIONADOR = 4;
  static final int DEFENSA = 5;

  // Dimensiones del campo y de las porterías
  private static final double ANCHO_CAMPO = 1.525;
  private static final double LONGITUD_CAMPO = 2.74;
  private static final double ANCHO_PORTERIA = 0.5;

  // Constantes para definir el campo de cada equipo
  private static final int EQUIPO_OESTE = -1;
  private static final int EQUIPO_ESTE = 1;

  // Variables del archivo .dsc (han de ser similares a las usadas)
  private static final int DSC_TIMEOUT = 600000;
  private static final int DSC_MAXTIMESTEP = 3;
  private static final int DSC_MAXTIMESTEP_DEFAULT = 50;

  // Límites de ciclos antes de decidir que el balón está bloqueado
  private static final int LIMITE_BLOQUEO1 = 50;
  private static final int LIMITE_BLOQUEO2 = 100;

  // Sirve para predecir la posición del balón en turnos futuros
  private static final int TURNOS_PREDICCION = 80;

  // Radio del robot
  private double RADIO_ROBOT = abstract_robot.RADIUS;

  // Márgenes a mantener para no chocar con paredes y robots
  private double MARGEN_CHOQUE_ROBOTS = 3 * RADIO_ROBOT;
  private double MARGEN_CHOQUE_PAREDES = RADIO_ROBOT * 1.1;

  // Variable para indicar el número de identificador del jugador
  int miNumero;

  // Tiempo para los timestamps
  long tiempo;

  // Tiempo transcurrido de ejecución (para evitar resets y reloads)
  static long tiempoTranscurrido = 0;

  // Variables para indicar el campo de nuestro equipo y del contrario
  int miCampo, suCampo;

  // Posiciones de las dos porterías (referidas al jugador)
  Vec2 miPorteria, suPorteria;

  // Posición absoluta del jugador (referida al centro del campo)
  Vec2 yoAbsoluta;

  // Posiciones absoluta y relativa del balón
  Vec2 balon, balonAbsoluta;

  // Posiciones absolutas y relativas de los compañeros y oponentes
  Vec2[] companeros, contrarios, companerosAbsoluta, contrariosAbsoluta;

  // Posición de la portería propia referida al balón
  Vec2 miPorteriaReferidaAlBalon;

  // Posiciones relativas de los córneres de nuestro campo
  Vec2 cornerInferior, cornerSuperior;

  // Posiciones relativa y absoluta del lugar donde se suele colocar el central
  Vec2 puntoJugadorCentral, puntoJugadorCentralAbsoluta;

  // Posiciones relativa y absoluta del punto para evitar el bloqueo del balón
  Vec2 puntoDesbloqueo, puntoDesbloqueoAbsoluta;

  // Estilos de juego disponibles
  static EstiloDeJuego relajado, posesivo, defensivo, ofensivo;

  // Estilo de juego actual
  static EstiloDeJuego estiloDeJuego;

  // Goles a favor y en contra
  static int golesEnContra, golesAFavor;

  // Historial de posiciones del balón
  static Historial historial;

  // Tiempo que el balón lleva bloqueado sin apenas moverse
  static long tiempoDeBloqueo;
  static long tiempoDeBloqueoTotal;

  // Cierto si el equipo recibe muchos goles y es necesario dejar de defender
  boolean remontando = false;

  /**
   * Método para configurar el sistema de control. Se invoca una sola vez para
   * inicializar el jugador
   */
  public void configure() {
    if (tiempoTranscurrido == 0) {
      golesAFavor = 0;
      golesEnContra = 0;
      historial = new Historial(30);
      tiempoDeBloqueo = 0;
      tiempoDeBloqueoTotal = 0;

      // Inicializa los diferentes estilos de juego
      inicializarEstilosDeJuego();

      // Establece el estilo ofensivo por defecto
      estiloDeJuego = ofensivo;
    }
  }

  /** Método invocado en cada paso de ejecución para permitir la ejecución del
   * sistema de control
   *
   * @return int Código de retorno
   */
  public int TakeStep() {

    // Guarda los resultados en la base de datos
    //Res.Resultado.Guardar(this,abstract_robot);

    // Obtiene el timestamp
    tiempo = abstract_robot.getTime();

    // Obtiene el número de identificación del jugador
    miNumero = abstract_robot.getPlayerNumber(tiempo);

    // Obtiene la posición absoluta del jugador
    yoAbsoluta = new Vec2(abstract_robot.getPosition(tiempo));

    // Obtiene las posiciones relativas del balón y las porterías
    balon = abstract_robot.getBall(tiempo);
    miPorteria = abstract_robot.getOurGoal(tiempo);
    suPorteria = abstract_robot.getOpponentsGoal(tiempo);

    // Obtiene las posiciones relativas de los compañeros y contrarios
    companeros = abstract_robot.getTeammates(tiempo);
    contrarios = abstract_robot.getOpponents(tiempo);

    // Ajusta estas posiciones para que señalen el centro de los jugadores
    for (int i = 0; i < companeros.length; i++)
      companeros[i].setr(companeros[i].r + RADIO_ROBOT);
    for (int i = 0; i < contrarios.length; i++)
      contrarios[i].setr(contrarios[i].r + RADIO_ROBOT);

    // Si algún equipo no tiene jugadores, no se hacen más cálculos
    if (contrarios.length == 0 || companeros.length == 0)
      return CSSTAT_OK;

    // Determina cuál es el campo del jugador
    miCampo = (abstract_robot.getOurGoal(tiempo).x < 0 ?
               EQUIPO_OESTE : EQUIPO_ESTE);
    suCampo = -miCampo;

    // Obtiene las posiciones absolutas de los compañeros y contrarios
    companerosAbsoluta = new Vec2[companeros.length];
    contrariosAbsoluta = new Vec2[contrarios.length];
    for (int i = 0; i < companeros.length; i++) {
      companerosAbsoluta[i] = new Vec2(companeros[i]);
      companerosAbsoluta[i].add(yoAbsoluta);
    }
    for (int i = 0; i < contrarios.length; i++) {
      contrariosAbsoluta[i] = new Vec2(contrarios[i]);
      contrariosAbsoluta[i].add(yoAbsoluta);
    }

    // Obtiene la posición absoluta del balón
    balonAbsoluta = new Vec2(balon);
    balonAbsoluta.add(yoAbsoluta);

    // Calcula la posición típica del jugador central (absoluta y relativa)
    puntoJugadorCentralAbsoluta = new Vec2(miCampo * abstract_robot.RADIUS * 3,
                                           abstract_robot.RADIUS / 2);
    puntoJugadorCentral = new Vec2(puntoJugadorCentralAbsoluta);
    puntoJugadorCentral.sub(yoAbsoluta);

    // Calcula la posición del punto de desbloqueo del balón
    puntoDesbloqueo = new Vec2(balon);
    puntoDesbloqueo.add(miPorteria);
    puntoDesbloqueo.setr(puntoDesbloqueo.r * .87);

    // Calcula la posición de la portería propia referida al balón
    miPorteriaReferidaAlBalon = new Vec2(miPorteria);
    miPorteriaReferidaAlBalon.sub(balon);

    // Calcula las posiciones relativas de los córneres
    cornerInferior = new Vec2(miPorteria.x, miPorteria.y - ANCHO_CAMPO / 2);
    cornerSuperior = new Vec2(miPorteria.x, miPorteria.y + ANCHO_CAMPO / 2);

    // Guarda la posición absoluta del jugador en un objeto EstiloDeJuego
    estiloDeJuego.roles[miNumero].jugadorAbsoluta = yoAbsoluta;

    // Se elige la táctica una vez por ciclo (sólo con el jugador número cero)
    if (miNumero == 0) {

      // Cuenta del tiempo transcurrido (para resets y reloads)
      if (tiempo == DSC_MAXTIMESTEP && tiempoTranscurrido > DSC_MAXTIMESTEP) {
        tiempoTranscurrido += 2 * DSC_MAXTIMESTEP;
        tiempoDeBloqueo = 0;
      }
      else
        tiempoTranscurrido += DSC_MAXTIMESTEP;

      // Añade la posición absoluta del balón al historial de posiciones
      historial.add(balonAbsoluta);

      // Si el balón no se ha movido demasiado desde el último turno, se
      // incrementan los tiempos de bloqueo
      if (historial.dameDesviacionMaxima() <=
          0.12 * (double) DSC_MAXTIMESTEP /
          (double) DSC_MAXTIMESTEP_DEFAULT) {
        tiempoDeBloqueo++;
        tiempoDeBloqueoTotal++;
      }
      else
        tiempoDeBloqueo = 0;

      // Actualiza los goles a favor y en contra (si se ha marcado algún gol)
      if (abstract_robot.getJustScored(tiempo) == 1) {
        golesAFavor++;
        estiloDeJuego.reset();
      }
      else if (abstract_robot.getJustScored(tiempo) == -1) {
        golesEnContra++;
        estiloDeJuego.reset();
      }

      // Si no se ha ido perdiendo de una cantidad importante de goles
      if (!remontando) {

        // Si se va ganando o hay empate, la táctica es relajada
        if (golesAFavor - golesEnContra >= 0) {
          if (estiloDeJuego != relajado)
            estiloDeJuego = relajado;
        }

        // Si se va perdiendo de 5 goles, la táctica deja de ser defensiva
        else if (golesAFavor - golesEnContra < -5) {
          if (estiloDeJuego != relajado)
            estiloDeJuego = relajado;
          remontando = true;
        }

        // Si se va perdiendo de 3 goles, la táctica es defensiva
        else if (golesAFavor - golesEnContra < -3) {
          if (estiloDeJuego != defensivo)
            estiloDeJuego = defensivo;
        }

        // Si se va perdiendo de un gol, la táctica es posesiva
        else if (golesAFavor - golesEnContra <= -1) {
          if (estiloDeJuego != posesivo)
            estiloDeJuego = posesivo;
        }

      }

    }

    // Establece el comportamiento de cada jugador según el tipo que sea
    switch (estiloDeJuego.roles[miNumero].tipoJugador) {

      // Actúa como bloqueador del portero contrario
      case BLOQUEADOR:
        actuaComoBloqueador();
        break;

      // Actúa como defensa presionador (menor distancia de intercepción)
      case DEFENSA_PRESIONADOR:
        actuaComoDefensa(.9, RADIO_ROBOT * 6);
        break;

      // Actúa como defensa normal
      case DEFENSA:
        actuaComoDefensa(.8, RADIO_ROBOT * 6);
        break;

      // Actúa como delantero
      case DELANTERO:
        actuaComoDelantero();
        break;

      // Actúa como central
      case CENTRAL:
        actuaComoCentral();
        break;
    }

    // Devuelve un código de retorno indicando éxito
    return (CSSTAT_OK);
  }

  /** Método que predice la posición del balón en un instante futuro a partir
   * del historial de posiciones
   *
   * @param incremento double Incremento del tiempo
   * @return Vec2 Predicción de la posición del balón cuando hayan transcurrido
   * un número de unidades de tiempo igual a incremento
   */
  private Vec2 predicePosicionBalonAbsoluta(double incremento) {

    // Si hay historial suficiente, hace la predicción
    if (historial.size() > 2) {

      // Las coordenadas del vector velocidad se obtienen calculando las
      // diferencias de posiciones de los dos instantes anteriores
      double velocidadX = ( (Vec2) historial.get(0)).x -
          ( (Vec2) historial.get(1)).x;
      double velocidadY = ( (Vec2) historial.get(0)).y -
          ( (Vec2) historial.get(1)).y;

      // Se devuelve la nueva posición predicha, usando la ecuación del
      // movimiento (x,y) = (x0,y0) + (vx,vy)*incremento
      return new Vec2(balonAbsoluta.x + velocidadX * incremento,
                      balonAbsoluta.y + velocidadY * incremento);
    }

    // Si no, devuelve la posición actual del balón
    else
      return balonAbsoluta;
  }

  /** Método que implementa el comportamiento de los defensas
   *
   * @param factorDistIntercepcion double Factor de la distancia de intercepción
   * del defensa (cuanto más cercano a 1, más se acerca al oponente / balón)
   * @param radioDeAccion double Radio de acción dentro del cual debe estar la
   * pelota para que le jugador actúe como defensa
   */
  private void actuaComoDefensa(double factorDistIntercepcion,
                                double radioDeAccion) {

    // Vector para establecer la trayectoria y orientación del defensa
    Vec2 resultado = new Vec2(0, 0);

    // Posición del balón con respecto al contrario más peligroso
    Vec2 balonReferidoAlContrario = new Vec2(Double.MAX_VALUE, Double.MAX_VALUE);

    // Posiciones absoluta y relativa del contrario más peligroso
    Vec2 contrario = new Vec2(contrarios[0]);
    Vec2 contrarioAbsoluta = new Vec2(contrariosAbsoluta[0]);

    // Si el balón está al alcance del jugador y no lo tiene ningún compañero
    // intenta hacerse con él, intercambiando su rol con el compañero que
    // está más lejos del balón y pasando a ser delantero
    if (estoyDetrasDelBalon() && !tieneElBalon(companeros) &&
        balon.r < radioDeAccion &&
        miPorteria.r < RADIO_ROBOT * 8) {
      estiloDeJuego.intercambiarRoles(miNumero, dameCompaneroMasLejosDelBalon());
      actuaComoDelantero();
    }
    else {

      // Encuentra al contrario más cercano al balón que lo lleve por delante
      for (int i = 0; i < contrariosAbsoluta.length; i++) {
        if (estaDetrasDelBalon(contrariosAbsoluta[i], suCampo)) {
          Vec2 tmp = new Vec2(balon);
          tmp.sub(contrarios[i]);
          if (tmp.r < balonReferidoAlContrario.r) {
            balonReferidoAlContrario = new Vec2(tmp);
            contrario = new Vec2(contrarios[i]);
            contrarioAbsoluta = new Vec2(contrariosAbsoluta[i]);
          }
        }
      }

      // Si el contrario va a tirar a gol, el defensa se interpone en su camino
      if (tieneTiro(contrarioAbsoluta, suCampo)) {

        // Halla el punto de intercepción entre el oponente y la portería,
        // teniendo en cuenta el factor de distancia de intercepción
        resultado = new Vec2(contrario);
        balonReferidoAlContrario.setr(miPorteriaReferidaAlBalon.r *
                                      factorDistIntercepcion);
        resultado.add(balonReferidoAlContrario);

        // Si el punto de intercepción del contrario está a un radio de
        // distancia, avanza para interceptarlo a la máxima velocidad
        if (resultado.r > RADIO_ROBOT) {
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, true,
                                        contrario);
          estableceVectorVelocidad(resultado.t, 1.0);
        }

        // Si el punto de intercepción del contrario está a un tercio de radio
        // de distancia, avanza para interceptarlo a un cuarto de velocidad
        else if (resultado.r > RADIO_ROBOT / 3) {
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, true,
                                        contrario);
          estableceVectorVelocidad(resultado.t, 0.25);
        }

        // Si el punto de intercepción del contrario está cerquísima, tan sólo
        // se orienta hacia el balón
        else {
          estableceVectorVelocidad(balon.t, 0.0);
        }
      }

      // Si no tiene tiro, el defensa se pone en el camino del balón
      else {

        // Halla el punto de intercepción entre el balón y la portería, teniendo
        // en cuenta el factor de distancia de intercepción
        resultado = new Vec2(miPorteria);
        resultado.sub(balon);
        resultado.setr(resultado.r * factorDistIntercepcion);
        resultado.add(balon);

        // Si el punto de intercepción del balón está lejos, va hacia él con
        // la máxima velocidad y esquivando paredes y robots
        if (resultado.r > RADIO_ROBOT * 2) {
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, true, null);
          estableceVectorVelocidad(resultado.t, 1.0);
        }

        // Si el punto de intercepción está cerca, tan sólo se orienta hacia el
        // balón
        else {
          estableceVectorVelocidad(balon.t, 0.0);
        }
      }
    }
  }

  /** Método que implementa el comportamiento de los centrales
   *
   */
  private void actuaComoCentral() {

    // Si el balón está bloqueado, contribuye a desbloquearlo
    if (estaBloqueado())
      return;

    // Si el jugador es el más cercano al balón, intercambia los roles con el
    // compañero más alejado y pasa a actuar como delantero
    if (soyElMasCercanoAlBalon()) {
      estiloDeJuego.intercambiarRoles(miNumero, dameCompaneroMasLejosDelBalon());
      actuaComoDelantero();
    }

    // Si no, actúa como central
    else {

      // En caso de que todos los delanteros estén al ataque pero delante del
      // balón (no lo tienen o lo han perdido), la situación es peligrosa y
      // el central actúa momentáneamente como defensa
      int delanterosTotales = 0;
      int delanterosNoUtiles = 0;
      for (int i = 0; i < 5; i++) {
        if (estiloDeJuego.roles[i].tipoJugador == DELANTERO) {
          delanterosTotales++;
          if (!estaDetrasDelBalon(estiloDeJuego.roles[i].jugadorAbsoluta,
                                  miCampo)) {
            delanterosNoUtiles++;
          }
        }
      }

      // Si todos los delanteros están arriba y el equipo no lleva el balón, se
      // actúa como defensa con un gran radio de acción y una distancia de
      // intercepción alejada del contrario / balón
      if (!tieneElBalon(companeros) && delanterosTotales == delanterosNoUtiles) {
        actuaComoDefensa(.55, RADIO_ROBOT * 12);
        return;
      }

      // Si no hay peligro, se queda en el centro a la espera de que haya
      // disparos en el reloj del motor de SoccerBots que evita bloqueos, o
      // tras un gol. El lugar donde espera se denomina puntoJugadorCentral, que
      // está situado en el centro del campo, ligeramente hacia arriba y hacia
      // el lado de la propia meta
      Vec2 resultado = new Vec2(puntoJugadorCentral);

      // Si está muy lejos del punto central, se dirige a ese punto a la máxima
      // velocidad, esquivando a los otros robots
      if (resultado.r > RADIO_ROBOT * 2) {
        resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false, null);
        estableceVectorVelocidad(resultado.t, 1.0);
      }

      // Si está cerca del punto central, se dirige a ese punto a la mitad de la
      // velocidad, esquivando a los otros robots
      else if (resultado.r > RADIO_ROBOT) {
        resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false, null);
        estableceVectorVelocidad(resultado.t, 0.5);
      }

      // Si está cerquísima del punto central, se dirige a ese punto a un cuarto
      // de la velocidad, esquivando a los otros robots
      else if (resultado.r > RADIO_ROBOT / 2) {
        estableceVectorVelocidad(resultado.t, 0.25);
      }

      // Si está en el centro ya, se queda a la espera del balón
      else {
        estableceVectorVelocidad(yoAbsoluta.t + PI, 0.0);
      }
    }
  }

  /** Método que implementa el comportamiento de los bloqueadores del portero
   * contrario
   *
   */
  private void actuaComoBloqueador() {

    // Posiciones del portero contrario y vector de trayectoria del jugador
    Vec2 suPortero, suPorteroAbsoluta, resultado;

    // Si el jugador es el más cercano al balón, intercambia los roles con el
    // compañero más alejado y pasa a actuar como delantero
    if (soyElMasCercanoAlBalon()) {
      estiloDeJuego.intercambiarRoles(miNumero, dameCompaneroMasLejosDelBalon());
      actuaComoDelantero();
    }

    // Si no, actúa como bloqueador
    else {

      // Encuentra al portero contrario (oponente más cercano a su portería y
      // entre los dos postes aproximadamente)
      suPortero = contrarios[0];
      double xMaxima = miCampo * LONGITUD_CAMPO;
      for (int i = 0; i < contrarios.length; i++) {
        Vec2 tmp = new Vec2(contrarios[i]);
        tmp.add(yoAbsoluta);
        if ( (miCampo == EQUIPO_OESTE ? tmp.x > xMaxima : tmp.x < xMaxima) &&
            (tmp.y > -ANCHO_PORTERIA / 2 - RADIO_ROBOT * 3) &&
            (tmp.y < ANCHO_PORTERIA / 2 + RADIO_ROBOT * 3)) {
          xMaxima = tmp.x;
          suPortero = contrarios[i];
        }
      }

      // Halla la posición absoluta del portero
      suPorteroAbsoluta = new Vec2(suPortero);
      suPorteroAbsoluta.add(yoAbsoluta);
      resultado = new Vec2(suPorteroAbsoluta);

      // Si el portero está muy cerca de su portería
      if (miCampo == EQUIPO_OESTE ?
          suPorteroAbsoluta.x > LONGITUD_CAMPO / 2 - RADIO_ROBOT * 5.5 :
          suPorteroAbsoluta.x < -LONGITUD_CAMPO / 2 + RADIO_ROBOT * 5.5) {

        // Calcula la posición absoluta de la portería contraria
        Vec2 suPorteriaAbsoluta = new Vec2(suPorteria);
        suPorteriaAbsoluta.add(yoAbsoluta);

        // Calcula el punto de intersección entre el portero y el centro de la
        // portería, para interceptar al portero (punto de bloqueo relativo)
        Vec2 puntoDeBloqueoDelPortero = new Vec2(suPorteriaAbsoluta);
        puntoDeBloqueoDelPortero.setr(puntoDeBloqueoDelPortero.r -
                                      RADIO_ROBOT * 1.5);
        puntoDeBloqueoDelPortero.sub(yoAbsoluta);

        // Se orienta hacia el punto del bloqueo del portero, evitando a todos
        // los robots menos al portero contrario
        Vec2 PorteroReferidoAlPuntoDeBloqueoDelPortero = new Vec2(suPortero);
        PorteroReferidoAlPuntoDeBloqueoDelPortero.sub(puntoDeBloqueoDelPortero);
        resultado = PorteroReferidoAlPuntoDeBloqueoDelPortero;
        resultado.add(puntoDeBloqueoDelPortero);
        resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false,
                                      suPortero);
        estableceVectorVelocidad(resultado.t, 1.0);
      }

      // Si el portero está muy lejos de la porteria, no lo bloquea y actúa
      // como delantero
      else {
        actuaComoDelantero();
      }
    }
  }

  /** Método que implementa el comportamiento de los delanteros
   *
   */
  private void actuaComoDelantero() {

    // Vector para la trayectoria y orientación del jugador
    Vec2 resultado = new Vec2();

    // Si el balón está bloqueado, contribuye a desbloquearlo
    if (estaBloqueado())
      return;

    // Calcula la posición ideal para hacer un tiro
    resultado = damePosicionDeTiro(balon, suPorteria);

    // Si la pelota está muy cerca del jugador
    if (balon.r <= RADIO_ROBOT + 0.01) {

      // Si puede chutar, está detrás del balón y la táctica es disparoFacil, lanza
      if (abstract_robot.canKick(tiempo) && (estoyDetrasDelBalon() &&
                                             (estiloDeJuego.disparoFacil))) {
        abstract_robot.kick(tiempo);
      }

      // Si el estilo de juego es de despejandoACorner y no está detrás del balón
      if (estiloDeJuego.despejandoACorner && !estoyDetrasDelBalon() &&
          (miCampo == EQUIPO_ESTE ?
           balonAbsoluta.x > 0 :
           balonAbsoluta.x < 0)) {

        // Calcula la posición del córner más cercano de despeje
        Vec2 despejandoACorner = new Vec2(balonAbsoluta.x > 0 ? LONGITUD_CAMPO / 2 :
                             -LONGITUD_CAMPO / 2,
                             balon.y > 0 ? ANCHO_CAMPO / 2 : -ANCHO_CAMPO / 2);
        despejandoACorner.sub(yoAbsoluta);

        // Obtiene la posición de tiro para despejar al córner
        resultado = damePosicionDeTiro(balon, despejandoACorner);

        // Establece la trayectoria para ir a despejar
        estableceVectorVelocidad(resultado.t, 1.0);
      }

    }

    // Si la pelota está lejos del jugador
    else {

      // Si no es estilo de juego predictivo o está detrás del balón y cerca
      if (!estiloDeJuego.predictivo ||
          (estoyDetrasDelBalon() && balon.r < RADIO_ROBOT * 3)) {

        // Se acerca al balón esquivando a los otros jugadores
        resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false, null);

      }

      // Si el estilo es predictivo o se está en una posición no orientada al balón
      else {

        // Calcula los turnos de predicción necesarios según lo lejos que esté
        // el jugador de la pelota
        double turnosDePrediccion = (balon.r / RADIO_ROBOT - 1) * 14;


        // Obtiene la posición en la que predice que estará la pelota dentro de
        // un número de turnos igual a turnosDePrediccion
        Vec2 prediccion = predicePosicionBalonAbsoluta(turnosDePrediccion);
        prediccion.sub(yoAbsoluta);

        // Si el jugador no está entre el balón y el punto de predicción
        if (!estoyEnMedio(prediccion, balon)) {

          // Ajusta la trayectoria para ir al punto de predicción
          Vec2 objetivo = ajustaTrayectoria(prediccion, MARGEN_CHOQUE_ROBOTS, false, null);

          // Si el camino hasta el punto de predicción está bloqueado, vuelve
          // a hacer una predicción, con más turnos de predicción esta vez
          if (objetivo.r != prediccion.r && !estoyDetrasDelBalon()) {
            turnosDePrediccion += TURNOS_PREDICCION;
            prediccion = predicePosicionBalonAbsoluta(turnosDePrediccion);
            prediccion.sub(yoAbsoluta);
          }

          // Se orienta hacia el punto de tiro entre el punto de predicción y la
          // puerta contraria
          resultado = ajustaTrayectoria(damePosicionDeTiro(prediccion,
              suPorteria),
                                        MARGEN_CHOQUE_ROBOTS, false, null);
        }
        else
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false, null);

      }

      // Si la táctica es conGuarda
      if (estiloDeJuego.conGuarda) {

        // Si el jugador no lleva el balón, pero sí algún compañero
        if (!soyElMasCercanoAlBalon() && tieneElBalon(companeros)) {

          // Obtiene posición absoluta del compañero que lleva el balón
          Vec2 companeroDelanteAbsoluta = new Vec2(estiloDeJuego.roles[
              dameCompaneroMasCercaDelBalon()].
              jugadorAbsoluta);

          // Calcula la posición del balón referida al compañero adelantado
          Vec2 balonReferidoACompaneroDelante = new Vec2(balonAbsoluta);
          balonReferidoACompaneroDelante.sub(companeroDelanteAbsoluta);

          // Calcula una posición lateral para seguir al compañero adelantado
          double rc = balonReferidoACompaneroDelante.y /
              balonReferidoACompaneroDelante.x;
          Vec2 posicionLateral = new Vec2(1, -1 / rc);
          posicionLateral.setr(RADIO_ROBOT * 2);
          posicionLateral.add(companeroDelanteAbsoluta);
          posicionLateral.sub(yoAbsoluta);
          resultado = posicionLateral;

          // Se orienta hacia esa posición lateral
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, true, null);
          estableceVectorVelocidad(resultado.t, 1.0);
        }
      }
    }

    estableceVectorVelocidad(resultado.t, 1.0);

  }

  /** Método para ajustar la posición del jugador hacia un objetivo dado
   *
   * @param objetivo Vec2 Objetivo hacia el que orientarse
   * @param rango double Márgenes a mantener entre obstáculos
   * @param evitaParedes boolean Cierto si hay que evitar choques con paredes
   * @param excepcion Vec2 Obstáculo con el que sí que hay que chocar (bloqueos)
   * @return Vec2 Posición del jugador orientado hacia su objetivo
   */
  private Vec2 ajustaTrayectoria(Vec2 objetivo, double rango,
                                 boolean evitaParedes,
                                 Vec2 excepcion) {

    // Vector para indicar orientación y trayectoria del jugador
    Vec2 resultado = new Vec2();

    // Arcos libres de obstáculos hacia los que orientarse
    Arcos arcosLibresDeObstaculos = new Arcos();

    // Variable temporal
    Arco arco = new Arco(0, 0);

    // Para todos los compañeros cercanos (salvo la excepción), elimina de la
    // lista de arcos libres aquellos correspondientes a la orientación del
    // jugador hacia los compañeros, para así poder esquivarlos
    for (int i = 0; i < companeros.length; i++) {
      if (companeros[i].r < rango && companeros[i] != excepcion) {
        arcosLibresDeObstaculos.eliminaArco(esquivaJugadores(companeros[i]));
      }
    }

    // Para todos los oponentes cercanos (salvo la excepción), elimina de la
    // lista de arcos libres aquellos correspondientes a la orientación del
    // jugador hacia los oponentes, para así poder esquivarlos
    for (int i = 0; i < contrarios.length; i++) {
      if (contrarios[i].r < rango && contrarios[i] != excepcion) {
        arcosLibresDeObstaculos.eliminaArco(esquivaJugadores(contrarios[i]));
      }
    }

    // Si hay que evitar paredes, elimina los arcos entre 90º y 270º, según la
    // pared que esté más cercana
    if (evitaParedes) {
      if (yoAbsoluta.x < ( -LONGITUD_CAMPO / 2 + MARGEN_CHOQUE_PAREDES) &&
          resultado.x < 0)
        arcosLibresDeObstaculos.eliminaArco(new Arco(1.5 * PI, .5 * PI));
      if (yoAbsoluta.x > (LONGITUD_CAMPO / 2 - MARGEN_CHOQUE_PAREDES) &&
          resultado.x > 0)
        arcosLibresDeObstaculos.eliminaArco(new Arco(.5 * PI, 1.5 * PI));
    }

    // Calcula si hay arcos con extremos en 0 y 2*pi radianes
    boolean derechoCero = false;
    boolean izquierdo2PI = false;
    for (int i = 0; i < arcosLibresDeObstaculos.size(); i++) {
      if ( ( (Arco) arcosLibresDeObstaculos.get(i)).derecho == 0)
        derechoCero = true;
      if ( ( (Arco) arcosLibresDeObstaculos.get(i)).izquierdo == PI2)
        izquierdo2PI = true;
    }

    // Obtiene en izquierdo y derecho aquellos ángulos adonde se puede orientar
    double izquierdo = objetivo.t;
    double derecho = objetivo.t;
    double intent = objetivo.t;
    for (int i = 0; i < arcosLibresDeObstaculos.size(); i++) {
      arco = (Arco) arcosLibresDeObstaculos.get(i);
      if (arco.seIncluye(intent)) {
        izquierdo = intent;
        derecho = intent;
        break;
      }
      else {
        if (!arco.seIncluye(derecho)) {
          if (! (arco.derecho == 0.0 && derechoCero && izquierdo2PI))
            derecho = arco.derecho;
        }
        if (!arco.seIncluye(izquierdo)) {
          if (! (arco.izquierdo == PI2 && derechoCero && izquierdo2PI))
            izquierdo = arco.izquierdo;
        }
      }
    }

    // Se orienta al que tenga menos diferencia con la orientación del objetivo
    if (diferenciaEntreAngulos(objetivo.t, izquierdo) <
        diferenciaEntreAngulos(objetivo.t, derecho)) {
      resultado.sett(izquierdo);
    }
    else {
      resultado.sett(derecho);
    }

    // Devuelve el vector con la orientación
    return resultado;
  }

  /** Método que determina si un jugador puede tirar a puerta
   *
   * @param jugadorAbsoluta Vec2 Posición del jugador
   * @param campoDelJugador int Campo del jugador
   * @return boolean Cierto si el jugador puede lanzar
   */
  private boolean tieneTiro(Vec2 jugadorAbsoluta, int campoDelJugador) {

    // Posiciones referidas al jugador de los dos postes y del balón
    Vec2 posteInferiorReferidoAlJugador, posteSuperiorReferidoAlJugador,
        balonReferidoAlJugador;

    // Variable temporal (arco de tiro)
    Arco arco;

    // Calcula las posiciones reltativas al jugador de los postes y del balón
    posteInferiorReferidoAlJugador = new Vec2( -campoDelJugador *
                                              LONGITUD_CAMPO / 2,
                                              -ANCHO_PORTERIA / 2);
    posteInferiorReferidoAlJugador.sub(jugadorAbsoluta);
    posteSuperiorReferidoAlJugador = new Vec2( -campoDelJugador *
                                              LONGITUD_CAMPO / 2,
                                              ANCHO_PORTERIA / 2);
    posteSuperiorReferidoAlJugador.sub(jugadorAbsoluta);
    balonReferidoAlJugador = new Vec2(balonAbsoluta);
    balonReferidoAlJugador.sub(jugadorAbsoluta);

    // Dibuja un arco de tiro entre los dos postes
    if (campoDelJugador == EQUIPO_ESTE)
      arco = new Arco(posteInferiorReferidoAlJugador.t,
                      posteSuperiorReferidoAlJugador.t);
    else
      arco = new Arco(posteSuperiorReferidoAlJugador.t,
                      posteInferiorReferidoAlJugador.t);

    // Devuelve cierto si la línea que une la pelota con el jugador está dentro
    // de este arco de tiro
    return arco.seIncluye(balonReferidoAlJugador.t);
  }

  /** Método que devuelve la posición ideal de tiro, trazando una trayectoria
   * desde el objetivo de tiro y el balón y situando la posición del tiro 0.9
   * radios por delante del balón en esa trayectoria
   *
   * @param balon Vec2 Posición del balón
   * @param objetivo Vec2 Posición del objetivo de tiro
   * @return Vec2 Posición de tiro detrás de la pelota y hacia el objetivo
   */
  private Vec2 damePosicionDeTiro(Vec2 balon, Vec2 objetivo) {
    Vec2 puntoDeTiro = new Vec2(balon);
    puntoDeTiro.sub(objetivo);
    puntoDeTiro.setr(RADIO_ROBOT * .9);
    puntoDeTiro.add(balon);
    return puntoDeTiro;
  }

  /** Método que sirve para determinar si un jugador está entre dos puntos
   *
   * @param v1 Vec2 Punto A
   * @param v2 Vec2 Punto B
   * @return boolean Cierto si el jugador está entre los puntos A y B
   */
  private boolean estoyEnMedio(Vec2 v1, Vec2 v2) {

    // Traza una recta entre los puntos A y B con ecuacion M: y = mx + y
    double m = (v1.y - v2.y) / (v1.x - v2.x);
    double n = v1.y - (v1.x * m);

    // Halla el punto de intersección de una recta perpendicular a M que pase
    // por la posición del jugador con la recta M
    double interseccionX = -n / (m + 1 / n);
    double interseccionY = ( -1 / m) * interseccionX;

    // Halla la distancia entre el jugador y el punto de intersección
    double dist = Math.sqrt(interseccionX * interseccionX +
                            interseccionY * interseccionY);

    // Si el punto de intersección no está entre los puntos A y B, el jugador
    // no se encuentra entre esos dos puntos
    if (v2.x >= v1.x) {
      if (interseccionX < v1.x || interseccionX > v2.x)
        return false;
    }
    else {
      if (interseccionX > v1.x || interseccionX < v2.x)
        return false;
    }

    if (v2.y >= v1.y) {
      if (interseccionY < v1.y || interseccionY > v2.y)
        return false;
    }
    else {
      if (interseccionY > v1.y || interseccionY < v2.y)
        return false;
    }

    // En otro caso, el jugador se encuentra entre esos dos puntos si la
    // distancia entre éste y el punto de intersección es menor que un radio
    return dist < RADIO_ROBOT;
  }

  /** Método para saber si el jugador está o no detrás del balón (un jugador
   * está detrás del balón si el balón se encuentra entre la porteria contraria
   * y el jugador)
   *
   * @return boolean Cierto si el jugador está detrás del balón
   */
  private boolean estoyDetrasDelBalon() {
    return miCampo == EQUIPO_OESTE ? balon.x - RADIO_ROBOT / 2 > 0 :
        balon.x + RADIO_ROBOT / 2 < 0;
  }

  /** Método que determina si un balón está bloqueado sin poder salir de esa
   * situación, reorientando al jugador para escapar del bloqueo
   *
   * @return boolean
   */
  private boolean estaBloqueado() {

    // Siempre y cuando la pelota esté lo suficientemente alejada de la puerta,
    // si el balón está cerca del jugador pero delante de él o se ha sobrepasado
    // el tiempo de bloqueo (la pelota ha estado demasiado tiempo si variar de
    // posición apenas), el jugador se reorienta hacia el punto de desbloqueo
    if (miPorteriaReferidaAlBalon.r > RADIO_ROBOT * 7 &&
        ( (tiempoDeBloqueo > LIMITE_BLOQUEO1 && !estoyDetrasDelBalon() &&
           balon.r <= RADIO_ROBOT + 0.02)
         || tiempoDeBloqueo > LIMITE_BLOQUEO2)) {

      estableceVectorVelocidad(ajustaTrayectoria(puntoDesbloqueo,
                                                 MARGEN_CHOQUE_ROBOTS, true, null).
                               t,
                               1.0);
      return true;
    }
    return false;
  }

  /** Método que determina si un jugador está detrás del balón
   *
   * @param jugadorAbsoluta Vec2 Posición absoluta del jugador
   * @param campo int Campo en el que juega el jugador
   * @return boolean Cierto si el jugador está detrás del balón
   */
  private boolean estaDetrasDelBalon(Vec2 jugadorAbsoluta, int campo) {
    return campo == EQUIPO_OESTE ? jugadorAbsoluta.x <= balonAbsoluta.x :
        jugadorAbsoluta.x >= balonAbsoluta.x;
  }

  /** Método que indica si el jugador es el más cercano al balón de su equipo
   *
   * @return boolean Cierto si el jugador es el más cercano al balón del equipo
   */
  private boolean soyElMasCercanoAlBalon() {
    double distMinima = balon.r;
    for (int i = 0; i < companeros.length; i++) {
      Vec2 tmp = new Vec2(balon);
      tmp.sub(companeros[i]);
      if (tmp.r <= distMinima)return false;
    }
    return true;
  }

  /** Método que determina si un equipo está en posesión del balón (se considera
   * que es así si hay algún jugador de ese equipo a una distancia menor que el
   * radio del jugador + 0.02)
   *
   * @param equipo Vec2[] Equipo
   * @return boolean Cierto si el equipo tiene el balón
   */
  private boolean tieneElBalon(Vec2[] equipo) {
    double distMinima = balon.r;
    for (int i = 0; i < equipo.length; i++) {
      Vec2 tmp = new Vec2(balon);
      tmp.sub(equipo[i]);
      if (tmp.r <= distMinima) distMinima = tmp.r;
    }
    return (distMinima < RADIO_ROBOT + 0.02);
  }

  /** Método que devuelve el número de identificación del jugador más cercano
   * al balón del equipo
   *
   * @return int Número del jugador del equipo más cercano al balón
   */
  private int dameCompaneroMasCercaDelBalon() {
    int numeroMasCercano = -1;
    double rMinimo = Double.MAX_VALUE;
    for (int i = 0; i < 5; i++) {
      if (estiloDeJuego.roles[i].tipoJugador == DELANTERO) {
        Vec2 tmp = new Vec2(balonAbsoluta);
        tmp.sub(estiloDeJuego.roles[i].jugadorAbsoluta);
        if (tmp.r < rMinimo) {
          rMinimo = tmp.r;
          numeroMasCercano = i;
        }
      }
    }
    return numeroMasCercano;
  }

  /** Método que devuelve el número de identificación del jugador más lejano
   * al balón del equipo
   *
   * @return int Número del jugador del equipo más lejano al balón
   */
  private int dameCompaneroMasLejosDelBalon() {
    int numeroMasLejano = -1;
    double rMaximo = Double.MIN_VALUE;
    for (int i = 0; i < 5; i++) {
      if (estiloDeJuego.roles[i].tipoJugador == DELANTERO) {
        Vec2 tmp = new Vec2(balonAbsoluta);
        tmp.sub(estiloDeJuego.roles[i].jugadorAbsoluta);
        if (tmp.r > rMaximo) {
          rMaximo = tmp.r;
          numeroMasLejano = i;
        }
      }
    }
    return numeroMasLejano;
  }

  /** Método que calcula un arco que indica el margen de ángulos por donde un
   * jugador no debe pasar si no quiere chocarse con otro que se especifica
   * como parámetro
   *
   * @param jugador Vec2 Jugador obstáculo
   * @return Arco Arco de choque con el jugador
   */
  private Arco esquivaJugadores(Vec2 jugador) {

    // Se calculan las tangentes internas comunes a las circunferencias de radio
    // igual a RADIO_ROBOT que tienen como centro la posición de los dos
    // jugadores. Estas tangentes cortan a las circunferencias en dos puntos
    // que permiten determinar dos ángulos que indican los dos extremos del
    // arco de choque
    double r = RADIO_ROBOT;
    double c = jugador.r;
    double d = 4 * r * r - c * c;
    double rc = (2 * r * Math.sqrt( -d)) / d;
    return new Arco(jugador.t - Math.atan2(rc, 1),
                    jugador.t + Math.atan2(rc, 1));
  }

  /** Método que inicializa los distintos estilos de juego disponibles
   *
   */
  private void inicializarEstilosDeJuego() {

    // Inicializa el estilo de juego relajado
    // Un defensa, un central, un bloqueador y dos delanteros
    // Los delanteros además predicen las posiciones de la pelota y juegan
    // con guarda
    relajado = new EstiloDeJuego(new Rol[] {
                                 new Rol(DEFENSA, 0),
                                 new Rol(CENTRAL, 1),
                                 new Rol(DELANTERO, 2),
                                 new Rol(DELANTERO, 3),
                                 new Rol(BLOQUEADOR, 4)}, true, false, true, false);

    // Inicializa el estilo de juego posesivo
    // Un defensa, un central y tres delanteros
    // Los delanteros además predicen las posiciones de la pelota y juegan
    // con guarda
    posesivo = new EstiloDeJuego(new Rol[] {
                                 new Rol(DEFENSA, 0),
                                 new Rol(CENTRAL, 1),
                                 new Rol(DELANTERO, 2),
                                 new Rol(DELANTERO, 3),
                                 new Rol(DELANTERO, 4)}, true, false, true, false);

    // Inicializa el estilo de juego defensivo
    // Dos defensas, un presionador, un central, y un delantero
    // Los delanteros juegan con guarda, despejan tanto hacia delante como hacia
    // el córner, si es necesario
    defensivo = new EstiloDeJuego(new Rol[] {
                                  new Rol(DEFENSA, 0),
                                  new Rol(DEFENSA_PRESIONADOR, 1),
                                  new Rol(DEFENSA, 2),
                                  new Rol(CENTRAL, 3),
                                  new Rol(DELANTERO, 4)}, true, true, false, true);

    // Inicializa el estilo de juego ofensivo
    // Un central, un bloqueador y tres delanteros
    // Los delanteros van a por el balón continuamente
    ofensivo = new EstiloDeJuego(new Rol[] {
                                 new Rol(CENTRAL, 0),
                                 new Rol(DELANTERO, 1),
                                 new Rol(DELANTERO, 2),
                                 new Rol(DELANTERO, 3),
                                 new Rol(BLOQUEADOR, 4)}, false, false, false, false);

  }

  /** Método para establecer el vector velocidad del jugador
   *
   * @param angulo double Ángulo de orientación del jugador
   * @param velocidad double Módulo de la velocidad del jugador
   */
  void estableceVectorVelocidad(double angulo, double velocidad) {
    abstract_robot.setSteerHeading(tiempo, angulo);
    abstract_robot.setSpeed(tiempo, velocidad);
  }

  /** Método para obtener una cadena corta representando un real de doble
   * precisión
   *
   * @param d double Número real a representar
   * @return String Cadena de caracteres representando al número real
   */
  static String truncarDouble(double d) {
    String r_str = String.valueOf(d);
    String minus = "";

    if (d < 0) minus = "-";
    if (r_str.length() > 4) {
      r_str = minus +
          r_str.substring(r_str.indexOf('.'), r_str.indexOf('.') + 4);
    }
    else {
      r_str = minus + r_str.substring(r_str.indexOf('.'), r_str.length());
    }
    return r_str;
  }

  /** Método que devuelve la diferencia en radianes entre dos ángulos
   *
   * @param alpha double Primer ángulo en radianes
   * @param beta double Segundo ángulo en radianes
   * @return double Diferencia entre los dos ángulos
   */
  static double diferenciaEntreAngulos(double alpha, double beta) {
    double dif = normaliza(normaliza(alpha) - normaliza(beta));
    if (dif > PI)
      dif = PI2 - dif;
    return normaliza(dif);
  }

  /** Método que devuelve un número indicando el cuadrante del ángulo que se le
   * pasa como parámetro (0 para el primer cuadrante, 3 para el cuarto)
   *
   * @param d double Ángulo en radianes
   * @return int Cuadrante donde se encuentra el ángulo
   */
  static int dameCuadrante(double d) {
    double t = normaliza(d);

    if (t >= PI / 2) {
      if (t >= PI) {
        if (t >= PI + PI / 2) {
          return 3;
        }
        else return 2;
      }
      else return 1;
    }
    else return 0;
  }

  /** Método para normalizar un ángulo
   *
   * @param t double Ángulo a normalizar
   * @return double Ángulo normalizado entre 0 y 2*pi radianes
   */
  static double normaliza(double t) {
    double u = t;
    while (u > PI2) u = u - PI2;
    while (u < 0) u = u + PI2;
    return u;
  }

  /**
   *
   * <p>Title: SoccerBots </p>
   *
   * <p>Description: Clase interna para historial de posiciones del balón </p>
   *
   * <p>Copyright: Copyright (c) 2006</p>
   *
   * @author Francisco Calderón Setién
   * - José María Vivas Rebuelta
   * - César Andrés Sánchez
   *
   * @version 1.0
   */
  public class Historial
      extends Vector {

    // Capacidad del vector de posiciones
    int capacidad;

    /** Método constructor
     *
     * @param capacidad int Capacidad del vector de posiciones
     */
    Historial(int capacidad) {
      super(capacidad);
      this.capacidad = capacidad;
    }

    /** Método para añadir una posición más al historial
     *
     * @param v Vec2 Posición del balón a añadir al historial
     */
    void add(Vec2 v) {
      super.add(0, v);
      if (size() == capacidad)
        remove(capacidad - 1);
    }

    /** Método que devuelve la mayor diferencia entre dos elementos cualquiera
     * del historial de posiciones del balón (la mayor diferencia en distancias
     * a la pelota)
     *
     * @return double Desviación máxima de las posiciones del historial
     */
    double dameDesviacionMaxima() {
      double mayor = 0;
      Vec2 tmp;

      for (int i = 0; i < capacidad && i < size(); i++) {
        for (int j = 0; j < capacidad && j < size(); j++) {
          tmp = new Vec2( (Vec2) get(i));
          tmp.sub( (Vec2) get(j));
          if (tmp.r > mayor)
            mayor = tmp.r;
        }
      }
      return mayor;
    }

  }

  /**
   *
   * <p>Title: SoccerBots </p>
   *
   * <p>Description: Clase interna para la lista de arcos que representan
   * ángulos de tiro y márgenes de movimiento </p>
   *
   * <p>Copyright: Copyright (c) 2006</p>
   *
   * @author Francisco Calderón Setién
   * - José María Vivas Rebuelta
   * - César Andrés Sánchez
   *
   * @version 1.0
   */
  class Arcos
      extends Vector {

    /** Método constructor
     *
     */
    Arcos() {
      super(20);
      add(new Arco(PI2, 0));
    }

    /** Método que elimina un arco de la lista de arcos, y todos los que estén
     * contenidos en el que se elimina, partiendo en dos aquellos que tienen
     * parte en común
     *
     * @param o Arco Arco a eliminar
     */
    void eliminaArco(Arco o) {

      // Si el arco está especificado de forma poco usual (sentido de las agujas
      // del reloj)
      if (o.derecho > o.izquierdo) {

        // Elimina el arco que va desde 0 radianes al extremo izquierdo
        eliminaArco(new Arco(o.izquierdo, 0));

        // Elimina el arco que va desde 2*pi radianes al extremo derecho
        eliminaArco(new Arco(PI2, o.derecho));

      }
      else {

        // Para cada arco de la lista
        for (int i = 0; i < size(); i++) {
          Arco e = (Arco) get(i);

          // Si el arco a borrar es mayor que el actual de la lista, lo borra
          if (o.izquierdo >= e.izquierdo && o.derecho <= e.derecho) {
            remove(i);
          }

          // Si el arco a borrar está contenido en el actual en parte,
          // parte el arco actual en dos y añade los dos arcos (que se
          // correspondenla con la parte no contenida en el arco a borrar)
          else if (o.izquierdo <= e.izquierdo && o.derecho >= e.derecho) {
            remove(i);
            if (e.izquierdo != o.izquierdo)
              add(new Arco(e.izquierdo, o.izquierdo));
            if (e.derecho != o.derecho)
              add(new Arco(o.derecho, e.derecho));
          }

          // Si la mitad del arco a borrar está contenida en el arco actual,
          // elimina sólo esta mitad del actual
          else if (o.izquierdo <= e.izquierdo && o.izquierdo >= e.derecho) {
            e.derecho = o.izquierdo;
            if (e.derecho == e.izquierdo)
              remove(i);
          }
          else if (o.derecho >= e.derecho && o.derecho <= e.izquierdo) {
            e.izquierdo = o.derecho;
            if (e.derecho == e.izquierdo)
              remove(i);
          }
        }
      }

    }

  }

  /**
   *
   * <p>Title: SoccerBots </p>
   *
   * <p>Description: Clase interna para los arcos para representar ángulos de
   * tiro y márgenes de movimiento </p>
   *
   * <p>Copyright: Copyright (c) 2006</p>
   *
   * @author Francisco Calderón Setién
   * - José María Vivas Rebuelta
   * - César Andrés Sánchez
   *
   * @version 1.0
   */
  class Arco {

    // Comienzo y fin del arco en radianes (generalmente en sentido contrario
    // de las agujas del reloj)
    public double derecho = 0;
    public double izquierdo = 0;

    /** Método constructor
     *
     * @param izquierdo double Extremo izquierdo del ángulo
     * @param derecho double Extremo derecho del ángulo
     */
    Arco(double izquierdo, double derecho) {
      this.izquierdo = normaliza(izquierdo);
      this.derecho = normaliza(derecho);
    }

    /** Método para determinar si un ángulo está incluido en un arco
     *
     * @param angulo double Ángulo en radianes
     * @return boolean Cierto si el ángulo está incluido en el arco
     */
    boolean seIncluye(double angulo) {
      double a = normaliza(angulo);

      if (derecho < izquierdo)
        return (a < izquierdo && a > derecho);

      switch (dameCuadrante(a)) {
        case 0:
          return a < izquierdo;
        case 3:
          return a > derecho;
        default:
          return (a < izquierdo || a > derecho);
      }
    }

  }

  /**
   *
   * <p>Title: SoccerBots </p>
   *
   * <p>Description: Clase interna para representar los roles de cada jugador
   * del equipo </p>
   *
   * <p>Copyright: Copyright (c) 2006</p>
   *
   * @author Francisco Calderón Setién
   * - José María Vivas Rebuelta
   * - César Andrés Sánchez
   *
   * @version 1.0
   */
  public class Rol {

    // Tipo de jugador
    int tipoJugador;

    // Número inicial
    int numeroInicio;

    // Posición absoluta del jugador
    Vec2 jugadorAbsoluta = new Vec2(0, 0);

    /** Método constructor
     *
     * @param tipoJugador int Tipo de jugador
     * @param numeroInicio int Número inicial
     */
    Rol(int tipoJugador, int numeroInicio) {
      this.tipoJugador = tipoJugador;
      this.numeroInicio = numeroInicio;
    }

    /** Método constructor
     *
     * @param r Rol Rol
     */
    Rol(Rol r) {
      this.tipoJugador = r.tipoJugador;
      this.numeroInicio = r.numeroInicio;
      this.jugadorAbsoluta = new Vec2(r.jugadorAbsoluta);
    }

  }

  /**
   *
   * <p>Title: SoccerBots </p>
   *
   * <p>Description: Clase interna para representar las diferentes estrategias
   * del equipo </p>
   *
   * <p>Copyright: Copyright (c) 2006</p>
   *
   * @author Francisco Calderón Setién
   * - José María Vivas Rebuelta
   * - César Andrés Sánchez
   *
   * @version 1.0
   */
  public class EstiloDeJuego {

    // Roles de los cinco jugadores
    Rol[] roles = new Rol[5];

    // Actitud del delantero que sigue a un compañero con balón desde el lateral
    boolean conGuarda;

    // Actitud del delantero que en cuanto tiene ocasión dispara a puerta
    boolean disparoFacil;

    // Actitud del delantero que trata de predecir la posición de la pelota
    boolean predictivo;

    // Actitud del delantero que despeja al córner si es necesario
    boolean despejandoACorner;

    /** Método constructor
     *
     * @param roles Rol[]
     * @param conGuarda boolean
     * @param disparoFacil boolean
     * @param predictivo boolean
     * @param despejandoACorner boolean
     */
    EstiloDeJuego(Rol[] roles, boolean conGuarda, boolean disparoFacil,
                  boolean predictivo, boolean despejandoACorner) {
      for (int i = 0; i < 5; i++) {
        this.roles[i] = new Rol(roles[i]);
      }

      this.conGuarda = conGuarda;
      this.disparoFacil = disparoFacil;
      this.predictivo = predictivo;
      this.despejandoACorner = despejandoACorner;

    }

    /** Método de reseteo
     *
     */
    void reset() {
      for (int i = 0; i < 5; i++) {
        if (roles[i].numeroInicio != i) {
          int j;
          for (j = 0; j < 5; j++) {
            if (roles[j].numeroInicio == i)
              break;
          }
          intercambiarRoles(i, j);
        }
      }
    }

    /** Método que intercambia los roles entre dos jugadores del mismo equipo
     *
     * @param a int Número del jugador A
     * @param b int Número del jugador B
     */
    void intercambiarRoles(int a, int b) {
      Rol tmp = roles[a];
      roles[a] = roles[b];
      roles[b] = tmp;
    }

  }

}
