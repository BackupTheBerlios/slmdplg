(defrule inicio
?a <- (inicio)
=>
(retract ?a)
(assert (percepcion))
)

(defrule percepcion
?a <- (percepcion)
=>
(assert(num_jugador(fetch "num_jugador")))
(assert(marcador(fetch "marcador")))
(assert(portero_bloqueado(fetch "portero_bloqueado")))
(assert(accion))
)

;; El jugador 0 sera el portero siempre
(defrule portero 
?p <-(accion)
(num_jugador 0)
=>
(retract ?p)
(store "jugada" 0)
)

;; Si nuestro portero esta bloqueado, y somos el jugador 1, nos movemos a una posicion
;; defensiva cerca del area para actual como posible portero. Si somos el jugador 4,
;; nos quedaremos arriba para intentar aprovechar un contraataque.
(defrule porterobloqueado (declare(salience 30))
(portero_bloqueado 1)
?p <-(accion)
(num_jugador 1)
=>
(retract ?p)
(store "jugada" 3)
)

;; Si vamos perdiendo o empate, lanzamos a todos los jugadores al ataque
(defrule ataque
?p <-(accion)
(marcador ?x)
(not(num_jugador 0))
=>
(if (< ?x 1) 
then (retract ?p)
(store "jugada" 1)
)
)

;; Si vamos ganando ponemos a todos los jugadores menos a uno en modo defensivo
(defrule cerrojazo1
?p <-(accion)
(marcador ?x)
(not(num_jugador 0))
(not(num_jugador 4))
=>
(if (> ?x 0)
then 
(retract ?p)
(store "jugada" 2)
)
)

;; Si vamos ganando ponemos al jugador 4 como único delantero
(defrule cerrojazo2
?p <-(accion)
(marcador ?x)
(num_jugador 4)
=>
(if (> ?x 0)
then 
(retract ?p)
(store "jugada" 4)
)
)




