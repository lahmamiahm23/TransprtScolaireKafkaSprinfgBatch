package com.example.transport.entitie.enumeration;

/**
 * Statut d'un arrêt de trajet (TripStop)
 */
public enum TripStopStatus {

    SCHEDULED,       // Arrêt planifié mais non encore commencé
    WAITING,         // Bus en approche / parent attendu
    ARRIVED,         // Bus arrivé chez le parent (nouveau statut pour tracking)
    COMPLETED,       // Enfant / parent pris en charge, arrêt terminé
    LATE_BOARDING,   // Embarquement tardif (pouvant générer pénalité)
    NO_SHOW,         // Parent absent ou non disponible
    CANCELLED        // Arrêt annulé
}

