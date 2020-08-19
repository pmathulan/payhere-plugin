import { WebPlugin } from '@capacitor/core';
import { PayherePluginPlugin } from './definitions';

export class PayherePluginWeb extends WebPlugin implements PayherePluginPlugin {
  constructor() {
    super({
      name: 'PayherePlugin',
      platforms: ['web'],
    });
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}

const PayherePlugin = new PayherePluginWeb();

export { PayherePlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(PayherePlugin);
