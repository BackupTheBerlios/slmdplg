;HECHOS INICIALES 
(deffacts valoresiniciales
(estrategia ninguna)
)

;FUNCION PARA LEER DATOS RECOGIENDOLOS DE JAVA
(deffunction leerDatos()
(fetch resultado)
(assert (resultado (fetch resultado)))
(fetch tiempoActual)
(assert (tiempoActual (fetch tiempoActual)))
(fetch goles_a_favor_ataque)
(assert (goles_a_favor_ataque (fetch goles_a_favor_ataque)))
(fetch goles_a_favor_empate)
(assert (goles_a_favor_empate (fetch goles_a_favor_empate)))
(fetch defendemos)
(assert (defendemos (fetch defendemos)))
(fetch goles_en_contra_defensa)
(assert (goles_en_contra_defensa (fetch goles_en_contra_defensa)))
(assert (datosLeidos si))
)

;FUNCION QUE CALCULA LA ESTRATEGIA A SEGUIR
(deffunction calculoEstrategia (?resultado ?tiempoActual ?goles_a_favor_ataque ?goles_a_favor_empate ?defendemos ?goles_en_contra_defensa)
(bind ?estrategia ninguna)
;empate
(if (= ?resultado 0) then
      (bind ?estrategia Empate)
      (if (> ?tiempoActual 450000) then
        (bind ?estrategia Ataque)
      )
      (if (> ?goles_a_favor_ataque (+ ?goles_a_favor_empate 1)) then
        (bind ?estrategia Ataque)
      )
)
;else  ;perdemos
(if (< ?resultado 0) then
      (bind ?estrategia Ataque)
      (if (> ?goles_a_favor_empate (+ ?goles_a_favor_ataque 2)) then
        (bind ?estrategia Empate)
      )
      (if (eq ?defendemos TRUE) then 
        (bind ?estrategia Empate)
      )
)
;else ;ganamos por 1 ó 2 goles
(if (or (= ?resultado 1) (= ?resultado 2)) then
      (bind ?estrategia Defensa)
      (if (eq ?defendemos FALSE) then
        (bind ?estrategia Empate)
      )
     
      (if (> ?tiempoActual 450000) then
       (bind ?estrategia Defensa)
      )
      (if (< ?tiempoActual 150000) then
        (bind ?estrategia Empate)
      )
)
;else ;ganamos por + de 2 goles
(if (> ?resultado 2) then
      (bind ?estrategia Defensa)
)
(assert (estrategia ?estrategia))
)

;REGLA QUE LLAMA A LA FUNCION LEER DATOS
(defrule Regla1
(declare (salience 60)) 
?x1 <- (estrategia ninguna)
=>
(leerDatos)
(retract ?x1)
)

;REGLA QUE RECOGE LAS VARIABLES Y LLAMA A LA FUNCION QUE CALCULA LA ESTRATEGIA
(defrule Llamar
(declare (salience 70)) 
?x1 <- (datosLeidos si)
(resultado ?resultado)
(tiempoActual ?tiempoActual)
(goles_a_favor_ataque ?goles_a_favor_ataque)
(goles_a_favor_empate ?goles_a_favor_empate)
(defendemos ?defendemos)
(goles_en_contra_defensa ?goles_en_contra_defensa)
=>
(retract ?x1)
(calculoEstrategia ?resultado ?tiempoActual ?goles_a_favor_ataque ?goles_a_favor_empate ?defendemos ?goles_en_contra_defensa)
(assert (pasar))
)



(deffunction pasa (?estrategia)
(store estrategia ?estrategia)
)

;REGLA QUE DEVUELVE A JAVA LA ESTRATEGIA
(defrule valores
(declare (salience 80)) 
?x1 <- (pasar)
(estrategia ?estrategia)
=>
(pasa ?estrategia)
(retract ?x1)
)

