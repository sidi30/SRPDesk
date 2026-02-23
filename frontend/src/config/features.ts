/** Centralised feature flags — controlled via Vite env variables (.env) */
export const FEATURES = {
  /** CRA Annex I checklist & requirements module */
  REQUIREMENTS: import.meta.env.VITE_FEATURE_REQUIREMENTS === 'true',

  /** AI Questionnaire parser & auto-fill module */
  AI_QUESTIONNAIRE: import.meta.env.VITE_FEATURE_AI_QUESTIONNAIRE === 'true',
} as const;
