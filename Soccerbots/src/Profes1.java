import java.util.Vector;
import EDU.gatech.cc.is.util.Vec2;
import EDU.gatech.cc.is.abstractrobot.*;

/**
 *
 * <p>Title: SoccerBots </p>
 * MODIFICADO: Este es el equipo MamboFC pero lo cambiamos para pas�rselo a
 * los alumnos de ISBC de 2007
 *
 * <p>Description: Clase que implementa una estrategia de un equipo de
 * SoccerBots heterogeneo, de asignaci�n de roles y t�cticas din�mica y
 * con historial de posiciones del bal�n </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * @author Francisco Calder�n Seti�n
 * - Jos� Mar�a Vivas Rebuelta
 * - C�sar Andr�s S�nchez
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

  // Dimensiones del campo y de las porter�as
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

  // L�mites de ciclos antes de decidir que el bal�n est� bloqueado
  private static final int LIMITE_BLOQUEO1 = 50;
  private static final int LIMITE_BLOQUEO2 = 100;

  // Sirve para predecir la posici�n del bal�n en turnos futuros
  private static final int TURNOS_PREDICCION = 80;

  // Radio del robot
  private double RADIO_ROBOT = abstract_robot.RADIUS;

  // M�rgenes a mantener para no chocar con paredes y robots
  private double MARGEN_CHOQUE_ROBOTS = 3 * RADIO_ROBOT;
  private double MARGEN_CHOQUE_PAREDES = RADIO_ROBOT * 1.1;

  // Variable para indicar el n�mero de identificador del jugador
  int miNumero;

  // Tiempo para los timestamps
  long tiempo;

  // Tiempo transcurrido de ejecuci�n (para evitar resets y reloads)
  static long tiempoTranscurrido = 0;

  // Variables para indicar el campo de nuestro equipo y del contrario
  int miCampo, suCampo;

  // Posiciones de las dos porter�as (referidas al jugador)
  Vec2 miPorteria, suPorteria;

  // Posici�n absoluta del jugador (referida al centro del campo)
  Vec2 yoAbsoluta;

  // Posiciones absoluta y relativa del bal�n
  Vec2 balon, balonAbsoluta;

  // Posiciones absolutas y relativas de los compa�eros y oponentes
  Vec2[] companeros, contrarios, companerosAbsoluta, contrariosAbsoluta;

  // Posici�n de la porter�a propia referida al bal�n
  Vec2 miPorteriaReferidaAlBalon;

  // Posiciones relativas de los c�rneres de nuestro campo
  Vec2 cornerInferior, cornerSuperior;

  // Posiciones relativa y absoluta del lugar donde se suele colocar el central
  Vec2 puntoJugadorCentral, puntoJugadorCentralAbsoluta;

  // Posiciones relativa y absoluta del punto para evitar el bloqueo del bal�n
  Vec2 puntoDesbloqueo, puntoDesbloqueoAbsoluta;

  // Estilos de juego disponibles
  static EstiloDeJuego relajado, posesivo, defensivo, ofensivo;

  // Estilo de juego actual
  static EstiloDeJuego estiloDeJuego;

  // Goles a favor y en contra
  static int golesEnContra, golesAFavor;

  // Historial de posiciones del bal�n
  static Historial historial;

  // Tiempo que el bal�n lleva bloqueado sin apenas moverse
  static long tiempoDeBloqueo;
  static long tiempoDeBloqueoTotal;

  // Cierto si el equipo recibe muchos goles y es necesario dejar de defender
  boolean remontando = false;

  /**
   * M�todo para configurar el sistema de control. Se invoca una sola vez para
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

  /** M�todo invocado en cada paso de ejecuci�n para permitir la ejecuci�n del
   * sistema de control
   *
   * @return int C�digo de retorno
   */
  public int TakeStep() {

    // Guarda los resultados en la base de datos
    //Res.Resultado.Guardar(this,abstract_robot);

    // Obtiene el timestamp
    tiempo = abstract_robot.getTime();

    // Obtiene el n�mero de identificaci�n del jugador
    miNumero = abstract_robot.getPlayerNumber(tiempo);

    // Obtiene la posici�n absoluta del jugador
    yoAbsoluta = new Vec2(abstract_robot.getPosition(tiempo));

    // Obtiene las posiciones relativas del bal�n y las porter�as
    balon = abstract_robot.getBall(tiempo);
    miPorteria = abstract_robot.getOurGoal(tiempo);
    suPorteria = abstract_robot.getOpponentsGoal(tiempo);

    // Obtiene las posiciones relativas de los compa�eros y contrarios
    companeros = abstract_robot.getTeammates(tiempo);
    contrarios = abstract_robot.getOpponents(tiempo);

    // Ajusta estas posiciones para que se�alen el centro de los jugadores
    for (int i = 0; i < companeros.length; i++)
      companeros[i].setr(companeros[i].r + RADIO_ROBOT);
    for (int i = 0; i < contrarios.length; i++)
      contrarios[i].setr(contrarios[i].r + RADIO_ROBOT);

    // Si alg�n equipo no tiene jugadores, no se hacen m�s c�lculos
    if (contrarios.length == 0 || companeros.length == 0)
      return CSSTAT_OK;

    // Determina cu�l es el campo del jugador
    miCampo = (abstract_robot.getOurGoal(tiempo).x < 0 ?
               EQUIPO_OESTE : EQUIPO_ESTE);
    suCampo = -miCampo;

    // Obtiene las posiciones absolutas de los compa�eros y contrarios
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

    // Obtiene la posici�n absoluta del bal�n
    balonAbsoluta = new Vec2(balon);
    balonAbsoluta.add(yoAbsoluta);

    // Calcula la posici�n t�pica del jugador central (absoluta y relativa)
    puntoJugadorCentralAbsoluta = new Vec2(miCampo * abstract_robot.RADIUS * 3,
                                           abstract_robot.RADIUS / 2);
    puntoJugadorCentral = new Vec2(puntoJugadorCentralAbsoluta);
    puntoJugadorCentral.sub(yoAbsoluta);

    // Calcula la posici�n del punto de desbloqueo del bal�n
    puntoDesbloqueo = new Vec2(balon);
    puntoDesbloqueo.add(miPorteria);
    puntoDesbloqueo.setr(puntoDesbloqueo.r * .87);

    // Calcula la posici�n de la porter�a propia referida al bal�n
    miPorteriaReferidaAlBalon = new Vec2(miPorteria);
    miPorteriaReferidaAlBalon.sub(balon);

    // Calcula las posiciones relativas de los c�rneres
    cornerInferior = new Vec2(miPorteria.x, miPorteria.y - ANCHO_CAMPO / 2);
    cornerSuperior = new Vec2(miPorteria.x, miPorteria.y + ANCHO_CAMPO / 2);

    // Guarda la posici�n absoluta del jugador en un objeto EstiloDeJuego
    estiloDeJuego.roles[miNumero].jugadorAbsoluta = yoAbsoluta;

    // Se elige la t�ctica una vez por ciclo (s�lo con el jugador n�mero cero)
    if (miNumero == 0) {

      // Cuenta del tiempo transcurrido (para resets y reloads)
      if (tiempo == DSC_MAXTIMESTEP && tiempoTranscurrido > DSC_MAXTIMESTEP) {
        tiempoTranscurrido += 2 * DSC_MAXTIMESTEP;
        tiempoDeBloqueo = 0;
      }
      else
        tiempoTranscurrido += DSC_MAXTIMESTEP;

      // A�ade la posici�n absoluta del bal�n al historial de posiciones
      historial.add(balonAbsoluta);

      // Si el bal�n no se ha movido demasiado desde el �ltimo turno, se
      // incrementan los tiempos de bloqueo
      if (historial.dameDesviacionMaxima() <=
          0.12 * (double) DSC_MAXTIMESTEP /
          (double) DSC_MAXTIMESTEP_DEFAULT) {
        tiempoDeBloqueo++;
        tiempoDeBloqueoTotal++;
      }
      else
        tiempoDeBloqueo = 0;

      // Actualiza los goles a favor y en contra (si se ha marcado alg�n gol)
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

        // Si se va ganando o hay empate, la t�ctica es relajada
        if (golesAFavor - golesEnContra >= 0) {
          if (estiloDeJuego != relajado)
            estiloDeJuego = relajado;
        }

        // Si se va perdiendo de 5 goles, la t�ctica deja de ser defensiva
        else if (golesAFavor - golesEnContra < -5) {
          if (estiloDeJuego != relajado)
            estiloDeJuego = relajado;
          remontando = true;
        }

        // Si se va perdiendo de 3 goles, la t�ctica es defensiva
        else if (golesAFavor - golesEnContra < -3) {
          if (estiloDeJuego != defensivo)
            estiloDeJuego = defensivo;
        }

        // Si se va perdiendo de un gol, la t�ctica es posesiva
        else if (golesAFavor - golesEnContra <= -1) {
          if (estiloDeJuego != posesivo)
            estiloDeJuego = posesivo;
        }

      }

    }

    // Establece el comportamiento de cada jugador seg�n el tipo que sea
    switch (estiloDeJuego.roles[miNumero].tipoJugador) {

      // Act�a como bloqueador del portero contrario
      case BLOQUEADOR:
        actuaComoBloqueador();
        break;

      // Act�a como defensa presionador (menor distancia de intercepci�n)
      case DEFENSA_PRESIONADOR:
        actuaComoDefensa(.9, RADIO_ROBOT * 6);
        break;

      // Act�a como defensa normal
      case DEFENSA:
        actuaComoDefensa(.8, RADIO_ROBOT * 6);
        break;

      // Act�a como delantero
      case DELANTERO:
        actuaComoDelantero();
        break;

      // Act�a como central
      case CENTRAL:
        actuaComoCentral();
        break;
    }

    // Devuelve un c�digo de retorno indicando �xito
    return (CSSTAT_OK);
  }

  /** M�todo que predice la posici�n del bal�n en un instante futuro a partir
   * del historial de posiciones
   *
   * @param incremento double Incremento del tiempo
   * @return Vec2 Predicci�n de la posici�n del bal�n cuando hayan transcurrido
   * un n�mero de unidades de tiempo igual a incremento
   */
  private Vec2 predicePosicionBalonAbsoluta(double incremento) {

    // Si hay historial suficiente, hace la predicci�n
    if (historial.size() > 2) {

      // Las coordenadas del vector velocidad se obtienen calculando las
      // diferencias de posiciones de los dos instantes anteriores
      double velocidadX = ( (Vec2) historial.get(0)).x -
          ( (Vec2) historial.get(1)).x;
      double velocidadY = ( (Vec2) historial.get(0)).y -
          ( (Vec2) historial.get(1)).y;

      // Se devuelve la nueva posici�n predicha, usando la ecuaci�n del
      // movimiento (x,y) = (x0,y0) + (vx,vy)*incremento
      return new Vec2(balonAbsoluta.x + velocidadX * incremento,
                      balonAbsoluta.y + velocidadY * incremento);
    }

    // Si no, devuelve la posici�n actual del bal�n
    else
      return balonAbsoluta;
  }

  /** M�todo que implementa el comportamiento de los defensas
   *
   * @param factorDistIntercepcion double Factor de la distancia de intercepci�n
   * del defensa (cuanto m�s cercano a 1, m�s se acerca al oponente / bal�n)
   * @param radioDeAccion double Radio de acci�n dentro del cual debe estar la
   * pelota para que le jugador act�e como defensa
   */
  private void actuaComoDefensa(double factorDistIntercepcion,
                                double radioDeAccion) {

    // Vector para establecer la trayectoria y orientaci�n del defensa
    Vec2 resultado = new Vec2(0, 0);

    // Posici�n del bal�n con respecto al contrario m�s peligroso
    Vec2 balonReferidoAlContrario = new Vec2(Double.MAX_VALUE, Double.MAX_VALUE);

    // Posiciones absoluta y relativa del contrario m�s peligroso
    Vec2 contrario = new Vec2(contrarios[0]);
    Vec2 contrarioAbsoluta = new Vec2(contrariosAbsoluta[0]);

    // Si el bal�n est� al alcance del jugador y no lo tiene ning�n compa�ero
    // intenta hacerse con �l, intercambiando su rol con el compa�ero que
    // est� m�s lejos del bal�n y pasando a ser delantero
    if (estoyDetrasDelBalon() && !tieneElBalon(companeros) &&
        balon.r < radioDeAccion &&
        miPorteria.r < RADIO_ROBOT * 8) {
      estiloDeJuego.intercambiarRoles(miNumero, dameCompaneroMasLejosDelBalon());
      actuaComoDelantero();
    }
    else {

      // Encuentra al contrario m�s cercano al bal�n que lo lleve por delante
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

        // Halla el punto de intercepci�n entre el oponente y la porter�a,
        // teniendo en cuenta el factor de distancia de intercepci�n
        resultado = new Vec2(contrario);
        balonReferidoAlContrario.setr(miPorteriaReferidaAlBalon.r *
                                      factorDistIntercepcion);
        resultado.add(balonReferidoAlContrario);

        // Si el punto de intercepci�n del contrario est� a un radio de
        // distancia, avanza para interceptarlo a la m�xima velocidad
        if (resultado.r > RADIO_ROBOT) {
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, true,
                                        contrario);
          estableceVectorVelocidad(resultado.t, 1.0);
        }

        // Si el punto de intercepci�n del contrario est� a un tercio de radio
        // de distancia, avanza para interceptarlo a un cuarto de velocidad
        else if (resultado.r > RADIO_ROBOT / 3) {
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, true,
                                        contrario);
          estableceVectorVelocidad(resultado.t, 0.25);
        }

        // Si el punto de intercepci�n del contrario est� cerqu�sima, tan s�lo
        // se orienta hacia el bal�n
        else {
          estableceVectorVelocidad(balon.t, 0.0);
        }
      }

      // Si no tiene tiro, el defensa se pone en el camino del bal�n
      else {

        // Halla el punto de intercepci�n entre el bal�n y la porter�a, teniendo
        // en cuenta el factor de distancia de intercepci�n
        resultado = new Vec2(miPorteria);
        resultado.sub(balon);
        resultado.setr(resultado.r * factorDistIntercepcion);
        resultado.add(balon);

        // Si el punto de intercepci�n del bal�n est� lejos, va hacia �l con
        // la m�xima velocidad y esquivando paredes y robots
        if (resultado.r > RADIO_ROBOT * 2) {
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, true, null);
          estableceVectorVelocidad(resultado.t, 1.0);
        }

        // Si el punto de intercepci�n est� cerca, tan s�lo se orienta hacia el
        // bal�n
        else {
          estableceVectorVelocidad(balon.t, 0.0);
        }
      }
    }
  }

  /** M�todo que implementa el comportamiento de los centrales
   *
   */
  private void actuaComoCentral() {

    // Si el bal�n est� bloqueado, contribuye a desbloquearlo
    if (estaBloqueado())
      return;

    // Si el jugador es el m�s cercano al bal�n, intercambia los roles con el
    // compa�ero m�s alejado y pasa a actuar como delantero
    if (soyElMasCercanoAlBalon()) {
      estiloDeJuego.intercambiarRoles(miNumero, dameCompaneroMasLejosDelBalon());
      actuaComoDelantero();
    }

    // Si no, act�a como central
    else {

      // En caso de que todos los delanteros est�n al ataque pero delante del
      // bal�n (no lo tienen o lo han perdido), la situaci�n es peligrosa y
      // el central act�a moment�neamente como defensa
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

      // Si todos los delanteros est�n arriba y el equipo no lleva el bal�n, se
      // act�a como defensa con un gran radio de acci�n y una distancia de
      // intercepci�n alejada del contrario / bal�n
      if (!tieneElBalon(companeros) && delanterosTotales == delanterosNoUtiles) {
        actuaComoDefensa(.55, RADIO_ROBOT * 12);
        return;
      }

      // Si no hay peligro, se queda en el centro a la espera de que haya
      // disparos en el reloj del motor de SoccerBots que evita bloqueos, o
      // tras un gol. El lugar donde espera se denomina puntoJugadorCentral, que
      // est� situado en el centro del campo, ligeramente hacia arriba y hacia
      // el lado de la propia meta
      Vec2 resultado = new Vec2(puntoJugadorCentral);

      // Si est� muy lejos del punto central, se dirige a ese punto a la m�xima
      // velocidad, esquivando a los otros robots
      if (resultado.r > RADIO_ROBOT * 2) {
        resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false, null);
        estableceVectorVelocidad(resultado.t, 1.0);
      }

      // Si est� cerca del punto central, se dirige a ese punto a la mitad de la
      // velocidad, esquivando a los otros robots
      else if (resultado.r > RADIO_ROBOT) {
        resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false, null);
        estableceVectorVelocidad(resultado.t, 0.5);
      }

      // Si est� cerqu�sima del punto central, se dirige a ese punto a un cuarto
      // de la velocidad, esquivando a los otros robots
      else if (resultado.r > RADIO_ROBOT / 2) {
        estableceVectorVelocidad(resultado.t, 0.25);
      }

      // Si est� en el centro ya, se queda a la espera del bal�n
      else {
        estableceVectorVelocidad(yoAbsoluta.t + PI, 0.0);
      }
    }
  }

  /** M�todo que implementa el comportamiento de los bloqueadores del portero
   * contrario
   *
   */
  private void actuaComoBloqueador() {

    // Posiciones del portero contrario y vector de trayectoria del jugador
    Vec2 suPortero, suPorteroAbsoluta, resultado;

    // Si el jugador es el m�s cercano al bal�n, intercambia los roles con el
    // compa�ero m�s alejado y pasa a actuar como delantero
    if (soyElMasCercanoAlBalon()) {
      estiloDeJuego.intercambiarRoles(miNumero, dameCompaneroMasLejosDelBalon());
      actuaComoDelantero();
    }

    // Si no, act�a como bloqueador
    else {

      // Encuentra al portero contrario (oponente m�s cercano a su porter�a y
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

      // Halla la posici�n absoluta del portero
      suPorteroAbsoluta = new Vec2(suPortero);
      suPorteroAbsoluta.add(yoAbsoluta);
      resultado = new Vec2(suPorteroAbsoluta);

      // Si el portero est� muy cerca de su porter�a
      if (miCampo == EQUIPO_OESTE ?
          suPorteroAbsoluta.x > LONGITUD_CAMPO / 2 - RADIO_ROBOT * 5.5 :
          suPorteroAbsoluta.x < -LONGITUD_CAMPO / 2 + RADIO_ROBOT * 5.5) {

        // Calcula la posici�n absoluta de la porter�a contraria
        Vec2 suPorteriaAbsoluta = new Vec2(suPorteria);
        suPorteriaAbsoluta.add(yoAbsoluta);

        // Calcula el punto de intersecci�n entre el portero y el centro de la
        // porter�a, para interceptar al portero (punto de bloqueo relativo)
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

      // Si el portero est� muy lejos de la porteria, no lo bloquea y act�a
      // como delantero
      else {
        actuaComoDelantero();
      }
    }
  }

  /** M�todo que implementa el comportamiento de los delanteros
   *
   */
  private void actuaComoDelantero() {

    // Vector para la trayectoria y orientaci�n del jugador
    Vec2 resultado = new Vec2();

    // Si el bal�n est� bloqueado, contribuye a desbloquearlo
    if (estaBloqueado())
      return;

    // Calcula la posici�n ideal para hacer un tiro
    resultado = damePosicionDeTiro(balon, suPorteria);

    // Si la pelota est� muy cerca del jugador
    if (balon.r <= RADIO_ROBOT + 0.01) {

      // Si puede chutar, est� detr�s del bal�n y la t�ctica es disparoFacil, lanza
      if (abstract_robot.canKick(tiempo) && (estoyDetrasDelBalon() &&
                                             (estiloDeJuego.disparoFacil))) {
        abstract_robot.kick(tiempo);
      }

      // Si el estilo de juego es de despejandoACorner y no est� detr�s del bal�n
      if (estiloDeJuego.despejandoACorner && !estoyDetrasDelBalon() &&
          (miCampo == EQUIPO_ESTE ?
           balonAbsoluta.x > 0 :
           balonAbsoluta.x < 0)) {

        // Calcula la posici�n del c�rner m�s cercano de despeje
        Vec2 despejandoACorner = new Vec2(balonAbsoluta.x > 0 ? LONGITUD_CAMPO / 2 :
                             -LONGITUD_CAMPO / 2,
                             balon.y > 0 ? ANCHO_CAMPO / 2 : -ANCHO_CAMPO / 2);
        despejandoACorner.sub(yoAbsoluta);

        // Obtiene la posici�n de tiro para despejar al c�rner
        resultado = damePosicionDeTiro(balon, despejandoACorner);

        // Establece la trayectoria para ir a despejar
        estableceVectorVelocidad(resultado.t, 1.0);
      }

    }

    // Si la pelota est� lejos del jugador
    else {

      // Si no es estilo de juego predictivo o est� detr�s del bal�n y cerca
      if (!estiloDeJuego.predictivo ||
          (estoyDetrasDelBalon() && balon.r < RADIO_ROBOT * 3)) {

        // Se acerca al bal�n esquivando a los otros jugadores
        resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false, null);

      }

      // Si el estilo es predictivo o se est� en una posici�n no orientada al bal�n
      else {

        // Calcula los turnos de predicci�n necesarios seg�n lo lejos que est�
        // el jugador de la pelota
        double turnosDePrediccion = (balon.r / RADIO_ROBOT - 1) * 14;


        // Obtiene la posici�n en la que predice que estar� la pelota dentro de
        // un n�mero de turnos igual a turnosDePrediccion
        Vec2 prediccion = predicePosicionBalonAbsoluta(turnosDePrediccion);
        prediccion.sub(yoAbsoluta);

        // Si el jugador no est� entre el bal�n y el punto de predicci�n
        if (!estoyEnMedio(prediccion, balon)) {

          // Ajusta la trayectoria para ir al punto de predicci�n
          Vec2 objetivo = ajustaTrayectoria(prediccion, MARGEN_CHOQUE_ROBOTS, false, null);

          // Si el camino hasta el punto de predicci�n est� bloqueado, vuelve
          // a hacer una predicci�n, con m�s turnos de predicci�n esta vez
          if (objetivo.r != prediccion.r && !estoyDetrasDelBalon()) {
            turnosDePrediccion += TURNOS_PREDICCION;
            prediccion = predicePosicionBalonAbsoluta(turnosDePrediccion);
            prediccion.sub(yoAbsoluta);
          }

          // Se orienta hacia el punto de tiro entre el punto de predicci�n y la
          // puerta contraria
          resultado = ajustaTrayectoria(damePosicionDeTiro(prediccion,
              suPorteria),
                                        MARGEN_CHOQUE_ROBOTS, false, null);
        }
        else
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, false, null);

      }

      // Si la t�ctica es conGuarda
      if (estiloDeJuego.conGuarda) {

        // Si el jugador no lleva el bal�n, pero s� alg�n compa�ero
        if (!soyElMasCercanoAlBalon() && tieneElBalon(companeros)) {

          // Obtiene posici�n absoluta del compa�ero que lleva el bal�n
          Vec2 companeroDelanteAbsoluta = new Vec2(estiloDeJuego.roles[
              dameCompaneroMasCercaDelBalon()].
              jugadorAbsoluta);

          // Calcula la posici�n del bal�n referida al compa�ero adelantado
          Vec2 balonReferidoACompaneroDelante = new Vec2(balonAbsoluta);
          balonReferidoACompaneroDelante.sub(companeroDelanteAbsoluta);

          // Calcula una posici�n lateral para seguir al compa�ero adelantado
          double rc = balonReferidoACompaneroDelante.y /
              balonReferidoACompaneroDelante.x;
          Vec2 posicionLateral = new Vec2(1, -1 / rc);
          posicionLateral.setr(RADIO_ROBOT * 2);
          posicionLateral.add(companeroDelanteAbsoluta);
          posicionLateral.sub(yoAbsoluta);
          resultado = posicionLateral;

          // Se orienta hacia esa posici�n lateral
          resultado = ajustaTrayectoria(resultado, MARGEN_CHOQUE_ROBOTS, true, null);
          estableceVectorVelocidad(resultado.t, 1.0);
        }
      }
    }

    estableceVectorVelocidad(resultado.t, 1.0);

  }

  /** M�todo para ajustar la posici�n del jugador hacia un objetivo dado
   *
   * @param objetivo Vec2 Objetivo hacia el que orientarse
   * @param rango double M�rgenes a mantener entre obst�culos
   * @param evitaParedes boolean Cierto si hay que evitar choques con paredes
   * @param excepcion Vec2 Obst�culo con el que s� que hay que chocar (bloqueos)
   * @return Vec2 Posici�n del jugador orientado hacia su objetivo
   */
  private Vec2 ajustaTrayectoria(Vec2 objetivo, double rango,
                                 boolean evitaParedes,
                                 Vec2 excepcion) {

    // Vector para indicar orientaci�n y trayectoria del jugador
    Vec2 resultado = new Vec2();

    // Arcos libres de obst�culos hacia los que orientarse
    Arcos arcosLibresDeObstaculos = new Arcos();

    // Variable temporal
    Arco arco = new Arco(0, 0);

    // Para todos los compa�eros cercanos (salvo la excepci�n), elimina de la
    // lista de arcos libres aquellos correspondientes a la orientaci�n del
    // jugador hacia los compa�eros, para as� poder esquivarlos
    for (int i = 0; i < companeros.length; i++) {
      if (companeros[i].r < rango && companeros[i] != excepcion) {
        arcosLibresDeObstaculos.eliminaArco(esquivaJugadores(companeros[i]));
      }
    }

    // Para todos los oponentes cercanos (salvo la excepci�n), elimina de la
    // lista de arcos libres aquellos correspondientes a la orientaci�n del
    // jugador hacia los oponentes, para as� poder esquivarlos
    for (int i = 0; i < contrarios.length; i++) {
      if (contrarios[i].r < rango && contrarios[i] != excepcion) {
        arcosLibresDeObstaculos.eliminaArco(esquivaJugadores(contrarios[i]));
      }
    }

    // Si hay que evitar paredes, elimina los arcos entre 90� y 270�, seg�n la
    // pared que est� m�s cercana
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

    // Obtiene en izquierdo y derecho aquellos �ngulos adonde se puede orientar
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

    // Se orienta al que tenga menos diferencia con la orientaci�n del objetivo
    if (diferenciaEntreAngulos(objetivo.t, izquierdo) <
        diferenciaEntreAngulos(objetivo.t, derecho)) {
      resultado.sett(izquierdo);
    }
    else {
      resultado.sett(derecho);
    }

    // Devuelve el vector con la orientaci�n
    return resultado;
  }

  /** M�todo que determina si un jugador puede tirar a puerta
   *
   * @param jugadorAbsoluta Vec2 Posici�n del jugador
   * @param campoDelJugador int Campo del jugador
   * @return boolean Cierto si el jugador puede lanzar
   */
  private boolean tieneTiro(Vec2 jugadorAbsoluta, int campoDelJugador) {

    // Posiciones referidas al jugador de los dos postes y del bal�n
    Vec2 posteInferiorReferidoAlJugador, posteSuperiorReferidoAlJugador,
        balonReferidoAlJugador;

    // Variable temporal (arco de tiro)
    Arco arco;

    // Calcula las posiciones reltativas al jugador de los postes y del bal�n
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

    // Devuelve cierto si la l�nea que une la pelota con el jugador est� dentro
    // de este arco de tiro
    return arco.seIncluye(balonReferidoAlJugador.t);
  }

  /** M�todo que devuelve la posici�n ideal de tiro, trazando una trayectoria
   * desde el objetivo de tiro y el bal�n y situando la posici�n del tiro 0.9
   * radios por delante del bal�n en esa trayectoria
   *
   * @param balon Vec2 Posici�n del bal�n
   * @param objetivo Vec2 Posici�n del objetivo de tiro
   * @return Vec2 Posici�n de tiro detr�s de la pelota y hacia el objetivo
   */
  private Vec2 damePosicionDeTiro(Vec2 balon, Vec2 objetivo) {
    Vec2 puntoDeTiro = new Vec2(balon);
    puntoDeTiro.sub(objetivo);
    puntoDeTiro.setr(RADIO_ROBOT * .9);
    puntoDeTiro.add(balon);
    return puntoDeTiro;
  }

  /** M�todo que sirve para determinar si un jugador est� entre dos puntos
   *
   * @param v1 Vec2 Punto A
   * @param v2 Vec2 Punto B
   * @return boolean Cierto si el jugador est� entre los puntos A y B
   */
  private boolean estoyEnMedio(Vec2 v1, Vec2 v2) {

    // Traza una recta entre los puntos A y B con ecuacion M: y = mx + y
    double m = (v1.y - v2.y) / (v1.x - v2.x);
    double n = v1.y - (v1.x * m);

    // Halla el punto de intersecci�n de una recta perpendicular a M que pase
    // por la posici�n del jugador con la recta M
    double interseccionX = -n / (m + 1 / n);
    double interseccionY = ( -1 / m) * interseccionX;

    // Halla la distancia entre el jugador y el punto de intersecci�n
    double dist = Math.sqrt(interseccionX * interseccionX +
                            interseccionY * interseccionY);

    // Si el punto de intersecci�n no est� entre los puntos A y B, el jugador
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
    // distancia entre �ste y el punto de intersecci�n es menor que un radio
    return dist < RADIO_ROBOT;
  }

  /** M�todo para saber si el jugador est� o no detr�s del bal�n (un jugador
   * est� detr�s del bal�n si el bal�n se encuentra entre la porteria contraria
   * y el jugador)
   *
   * @return boolean Cierto si el jugador est� detr�s del bal�n
   */
  private boolean estoyDetrasDelBalon() {
    return miCampo == EQUIPO_OESTE ? balon.x - RADIO_ROBOT / 2 > 0 :
        balon.x + RADIO_ROBOT / 2 < 0;
  }

  /** M�todo que determina si un bal�n est� bloqueado sin poder salir de esa
   * situaci�n, reorientando al jugador para escapar del bloqueo
   *
   * @return boolean
   */
  private boolean estaBloqueado() {

    // Siempre y cuando la pelota est� lo suficientemente alejada de la puerta,
    // si el bal�n est� cerca del jugador pero delante de �l o se ha sobrepasado
    // el tiempo de bloqueo (la pelota ha estado demasiado tiempo si variar de
    // posici�n apenas), el jugador se reorienta hacia el punto de desbloqueo
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

  /** M�todo que determina si un jugador est� detr�s del bal�n
   *
   * @param jugadorAbsoluta Vec2 Posici�n absoluta del jugador
   * @param campo int Campo en el que juega el jugador
   * @return boolean Cierto si el jugador est� detr�s del bal�n
   */
  private boolean estaDetrasDelBalon(Vec2 jugadorAbsoluta, int campo) {
    return campo == EQUIPO_OESTE ? jugadorAbsoluta.x <= balonAbsoluta.x :
        jugadorAbsoluta.x >= balonAbsoluta.x;
  }

  /** M�todo que indica si el jugador es el m�s cercano al bal�n de su equipo
   *
   * @return boolean Cierto si el jugador es el m�s cercano al bal�n del equipo
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

  /** M�todo que determina si un equipo est� en posesi�n del bal�n (se considera
   * que es as� si hay alg�n jugador de ese equipo a una distancia menor que el
   * radio del jugador + 0.02)
   *
   * @param equipo Vec2[] Equipo
   * @return boolean Cierto si el equipo tiene el bal�n
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

  /** M�todo que devuelve el n�mero de identificaci�n del jugador m�s cercano
   * al bal�n del equipo
   *
   * @return int N�mero del jugador del equipo m�s cercano al bal�n
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

  /** M�todo que devuelve el n�mero de identificaci�n del jugador m�s lejano
   * al bal�n del equipo
   *
   * @return int N�mero del jugador del equipo m�s lejano al bal�n
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

  /** M�todo que calcula un arco que indica el margen de �ngulos por donde un
   * jugador no debe pasar si no quiere chocarse con otro que se especifica
   * como par�metro
   *
   * @param jugador Vec2 Jugador obst�culo
   * @return Arco Arco de choque con el jugador
   */
  private Arco esquivaJugadores(Vec2 jugador) {

    // Se calculan las tangentes internas comunes a las circunferencias de radio
    // igual a RADIO_ROBOT que tienen como centro la posici�n de los dos
    // jugadores. Estas tangentes cortan a las circunferencias en dos puntos
    // que permiten determinar dos �ngulos que indican los dos extremos del
    // arco de choque
    double r = RADIO_ROBOT;
    double c = jugador.r;
    double d = 4 * r * r - c * c;
    double rc = (2 * r * Math.sqrt( -d)) / d;
    return new Arco(jugador.t - Math.atan2(rc, 1),
                    jugador.t + Math.atan2(rc, 1));
  }

  /** M�todo que inicializa los distintos estilos de juego disponibles
   *
   */
  private void inicializarEstilosDeJuego() {

    // Inicializa el estilo de juego relajado
    // Un defensa, un central, un bloqueador y dos delanteros
    // Los delanteros adem�s predicen las posiciones de la pelota y juegan
    // con guarda
    relajado = new EstiloDeJuego(new Rol[] {
                                 new Rol(DEFENSA, 0),
                                 new Rol(CENTRAL, 1),
                                 new Rol(DELANTERO, 2),
                                 new Rol(DELANTERO, 3),
                                 new Rol(BLOQUEADOR, 4)}, true, false, true, false);

    // Inicializa el estilo de juego posesivo
    // Un defensa, un central y tres delanteros
    // Los delanteros adem�s predicen las posiciones de la pelota y juegan
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
    // el c�rner, si es necesario
    defensivo = new EstiloDeJuego(new Rol[] {
                                  new Rol(DEFENSA, 0),
                                  new Rol(DEFENSA_PRESIONADOR, 1),
                                  new Rol(DEFENSA, 2),
                                  new Rol(CENTRAL, 3),
                                  new Rol(DELANTERO, 4)}, true, true, false, true);

    // Inicializa el estilo de juego ofensivo
    // Un central, un bloqueador y tres delanteros
    // Los delanteros van a por el bal�n continuamente
    ofensivo = new EstiloDeJuego(new Rol[] {
                                 new Rol(CENTRAL, 0),
                                 new Rol(DELANTERO, 1),
                                 new Rol(DELANTERO, 2),
                                 new Rol(DELANTERO, 3),
                                 new Rol(BLOQUEADOR, 4)}, false, false, false, false);

  }

  /** M�todo para establecer el vector velocidad del jugador
   *
   * @param angulo double �ngulo de orientaci�n del jugador
   * @param velocidad double M�dulo de la velocidad del jugador
   */
  void estableceVectorVelocidad(double angulo, double velocidad) {
    abstract_robot.setSteerHeading(tiempo, angulo);
    abstract_robot.setSpeed(tiempo, velocidad);
  }

  /** M�todo para obtener una cadena corta representando un real de doble
   * precisi�n
   *
   * @param d double N�mero real a representar
   * @return String Cadena de caracteres representando al n�mero real
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

  /** M�todo que devuelve la diferencia en radianes entre dos �ngulos
   *
   * @param alpha double Primer �ngulo en radianes
   * @param beta double Segundo �ngulo en radianes
   * @return double Diferencia entre los dos �ngulos
   */
  static double diferenciaEntreAngulos(double alpha, double beta) {
    double dif = normaliza(normaliza(alpha) - normaliza(beta));
    if (dif > PI)
      dif = PI2 - dif;
    return normaliza(dif);
  }

  /** M�todo que devuelve un n�mero indicando el cuadrante del �ngulo que se le
   * pasa como par�metro (0 para el primer cuadrante, 3 para el cuarto)
   *
   * @param d double �ngulo en radianes
   * @return int Cuadrante donde se encuentra el �ngulo
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

  /** M�todo para normalizar un �ngulo
   *
   * @param t double �ngulo a normalizar
   * @return double �ngulo normalizado entre 0 y 2*pi radianes
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
   * <p>Description: Clase interna para historial de posiciones del bal�n </p>
   *
   * <p>Copyright: Copyright (c) 2006</p>
   *
   * @author Francisco Calder�n Seti�n
   * - Jos� Mar�a Vivas Rebuelta
   * - C�sar Andr�s S�nchez
   *
   * @version 1.0
   */
  public class Historial
      extends Vector {

    // Capacidad del vector de posiciones
    int capacidad;

    /** M�todo constructor
     *
     * @param capacidad int Capacidad del vector de posiciones
     */
    Historial(int capacidad) {
      super(capacidad);
      this.capacidad = capacidad;
    }

    /** M�todo para a�adir una posici�n m�s al historial
     *
     * @param v Vec2 Posici�n del bal�n a a�adir al historial
     */
    void add(Vec2 v) {
      super.add(0, v);
      if (size() == capacidad)
        remove(capacidad - 1);
    }

    /** M�todo que devuelve la mayor diferencia entre dos elementos cualquiera
     * del historial de posiciones del bal�n (la mayor diferencia en distancias
     * a la pelota)
     *
     * @return double Desviaci�n m�xima de las posiciones del historial
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
   * �ngulos de tiro y m�rgenes de movimiento </p>
   *
   * <p>Copyright: Copyright (c) 2006</p>
   *
   * @author Francisco Calder�n Seti�n
   * - Jos� Mar�a Vivas Rebuelta
   * - C�sar Andr�s S�nchez
   *
   * @version 1.0
   */
  class Arcos
      extends Vector {

    /** M�todo constructor
     *
     */
    Arcos() {
      super(20);
      add(new Arco(PI2, 0));
    }

    /** M�todo que elimina un arco de la lista de arcos, y todos los que est�n
     * contenidos en el que se elimina, partiendo en dos aquellos que tienen
     * parte en com�n
     *
     * @param o Arco Arco a eliminar
     */
    void eliminaArco(Arco o) {

      // Si el arco est� especificado de forma poco usual (sentido de las agujas
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

          // Si el arco a borrar est� contenido en el actual en parte,
          // parte el arco actual en dos y a�ade los dos arcos (que se
          // correspondenla con la parte no contenida en el arco a borrar)
          else if (o.izquierdo <= e.izquierdo && o.derecho >= e.derecho) {
            remove(i);
            if (e.izquierdo != o.izquierdo)
              add(new Arco(e.izquierdo, o.izquierdo));
            if (e.derecho != o.derecho)
              add(new Arco(o.derecho, e.derecho));
          }

          // Si la mitad del arco a borrar est� contenida en el arco actual,
          // elimina s�lo esta mitad del actual
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
   * <p>Description: Clase interna para los arcos para representar �ngulos de
   * tiro y m�rgenes de movimiento </p>
   *
   * <p>Copyright: Copyright (c) 2006</p>
   *
   * @author Francisco Calder�n Seti�n
   * - Jos� Mar�a Vivas Rebuelta
   * - C�sar Andr�s S�nchez
   *
   * @version 1.0
   */
  class Arco {

    // Comienzo y fin del arco en radianes (generalmente en sentido contrario
    // de las agujas del reloj)
    public double derecho = 0;
    public double izquierdo = 0;

    /** M�todo constructor
     *
     * @param izquierdo double Extremo izquierdo del �ngulo
     * @param derecho double Extremo derecho del �ngulo
     */
    Arco(double izquierdo, double derecho) {
      this.izquierdo = normaliza(izquierdo);
      this.derecho = normaliza(derecho);
    }

    /** M�todo para determinar si un �ngulo est� incluido en un arco
     *
     * @param angulo double �ngulo en radianes
     * @return boolean Cierto si el �ngulo est� incluido en el arco
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
   * @author Francisco Calder�n Seti�n
   * - Jos� Mar�a Vivas Rebuelta
   * - C�sar Andr�s S�nchez
   *
   * @version 1.0
   */
  public class Rol {

    // Tipo de jugador
    int tipoJugador;

    // N�mero inicial
    int numeroInicio;

    // Posici�n absoluta del jugador
    Vec2 jugadorAbsoluta = new Vec2(0, 0);

    /** M�todo constructor
     *
     * @param tipoJugador int Tipo de jugador
     * @param numeroInicio int N�mero inicial
     */
    Rol(int tipoJugador, int numeroInicio) {
      this.tipoJugador = tipoJugador;
      this.numeroInicio = numeroInicio;
    }

    /** M�todo constructor
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
   * @author Francisco Calder�n Seti�n
   * - Jos� Mar�a Vivas Rebuelta
   * - C�sar Andr�s S�nchez
   *
   * @version 1.0
   */
  public class EstiloDeJuego {

    // Roles de los cinco jugadores
    Rol[] roles = new Rol[5];

    // Actitud del delantero que sigue a un compa�ero con bal�n desde el lateral
    boolean conGuarda;

    // Actitud del delantero que en cuanto tiene ocasi�n dispara a puerta
    boolean disparoFacil;

    // Actitud del delantero que trata de predecir la posici�n de la pelota
    boolean predictivo;

    // Actitud del delantero que despeja al c�rner si es necesario
    boolean despejandoACorner;

    /** M�todo constructor
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

    /** M�todo de reseteo
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

    /** M�todo que intercambia los roles entre dos jugadores del mismo equipo
     *
     * @param a int N�mero del jugador A
     * @param b int N�mero del jugador B
     */
    void intercambiarRoles(int a, int b) {
      Rol tmp = roles[a];
      roles[a] = roles[b];
      roles[b] = tmp;
    }

  }

}
