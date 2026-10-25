const BRASILIA_DATE = new Intl.DateTimeFormat('en-CA', {
  timeZone: 'America/Sao_Paulo',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
});

/** The yyyy-MM-dd that many days from today in Brasília, where the server counts its today. */
export function isoDaysFromToday(days: number): string {
  const parts = BRASILIA_DATE.formatToParts(new Date());
  const part = (type: Intl.DateTimeFormatPartTypes) =>
    Number(parts.find((p) => p.type === type)?.value);
  return new Date(Date.UTC(part('year'), part('month') - 1, part('day') + days))
    .toISOString()
    .slice(0, 10);
}
