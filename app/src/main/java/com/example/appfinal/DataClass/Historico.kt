package com.example.appfinal.DataClass

data class Historico(
    val data: String,
    val hora: String,
    val status: Boolean,
    val similaridadePorcentagem: Double = 0.0,
    val descricaoPadrao: String = "",
    val imagemSimilar: String = "",
    val distanciaEuclidiana: Double = 0.0
) {
    // Construtor secundário para compatibilidade com dados antigos
    constructor(
        data: String,
        hora: String,
        status: Boolean
    ) : this(
        data = data,
        hora = hora,
        status = status,
        similaridadePorcentagem = 0.0,
        descricaoPadrao = if (status) "Tendência de ALTA" else "Tendência de BAIXA",
        imagemSimilar = "",
        distanciaEuclidiana = 0.0
    )
}