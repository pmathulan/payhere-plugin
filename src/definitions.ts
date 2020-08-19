declare module '@capacitor/core' {
  interface PluginRegistry {
    PayherePlugin: PayherePluginPlugin;
  }
}

export interface PayherePluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
