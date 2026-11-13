// Espelho de breakpoints.css, para o que precisa de matchMedia no JavaScript.
export const BREAKPOINT = { sm: 640, md: 768, lg: 1024, xl: 1280 } as const

export const mediaQuery = (nome: keyof typeof BREAKPOINT): string =>
  `(min-width: ${BREAKPOINT[nome]}px)`
