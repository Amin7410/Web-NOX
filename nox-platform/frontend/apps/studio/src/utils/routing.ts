/**
 * Snaps a value to the 40px grid
 */
export const snapToGrid = (val: number): number => {
  return Math.round(val / 40) * 40;
};
