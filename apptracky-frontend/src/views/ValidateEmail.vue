<template>
  <v-container class="fill-height d-flex align-center justify-center">
    <v-card
      class="text-center"
      title="AppTracky"
      min-width="500px"
    >
      <v-card-item class="d-flex justify-center">
        <v-img :width="200" :src="error ? '/validateEmailError.webp' : '/validateEmailSuccess.webp'"></v-img>
      </v-card-item>
      <v-card-item>
        <v-alert type="success" v-if="!error">
          Success! Your account has been vaildated.
        </v-alert>
        <v-alert type="error" v-else>
          {{ error }}
        </v-alert>
      </v-card-item>
    </v-card>
  </v-container>
</template>

<script lang="ts">
import { postCustom } from '@/utils/fetch';
import { ref } from 'vue';

export default {
  setup() {
    const error = ref('');

    return {
      error
    };
  },
  beforeRouteEnter: async function(to, _from, next) {
    const uuid = to.query.uuid;
    if (!uuid) {
      next();
      return;
    }

    try {
      await postCustom('api/auth/validate-email', uuid);
      next();
    } catch (ex: any) {
      next(vm => {
        (vm as any).error = ex.message
      });
    }
  }
};
</script>
