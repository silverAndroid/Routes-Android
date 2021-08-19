type PromiseFn<T> = () => Promise<T>;

export async function promisesConcat<T>(
  promises: PromiseFn<any>[]
): Promise<T> {
  let result: T | null = null;
  for (const fn of promises) {
    result = await fn();
  }
  return result!;
}
