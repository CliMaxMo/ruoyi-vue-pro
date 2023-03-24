<template>

  <div :class="prefixCls"
  class="h-[100%] relative <xl:bg-v-dark <sm:px-10px <xl:px-10px <md:px-10px">

    <div :class="`${prefixCls}__left flex-1 bg-gray-500 bg-opacity-20 relative p-30px <xl:hidden`">

      
      <div>
        <img  src="" class="w-48px h-48px mr-10px" />
        <span class="text-20px font-bold">{{ underlineToHump(appStore.getTitle) }}</span>

      </div>

      <div>
        <transition-group tag="" name="">

          <img src="" alt="" key="1" class="w-350px"/>
          <div key="2">{{t('login.welcome')}}</div>
          <div class="mt-5 font-normal text-white text-14px" key="3">
              {{ t('login.message') }}
            </div>
        </transition-group>


      </div>
      

     
     
    </div>

  

  </div>




</template>

<script setup lang="ts">
import { underlineToHump } from '@/utils'

import { useDesign } from '@/hooks/web/useDesign'
import { useAppStore } from '@/store/modules/app'
import { ThemeSwitch } from '@/layout/components/ThemeSwitch'
import { LocaleDropdown } from '@/layout/components/LocaleDropdown'
import { LoginForm, MobileForm, RegisterForm, QrCodeForm } from './components'

const { t } = useI18n()
const appStore = useAppStore()
const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('login')
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-login;

.#{$prefix-cls} {
  &__left {
    &::before {
      position: absolute;
      top: 0;
      left: 0;
      z-index: -1;
      width: 100%;
      height: 100%;
      background-image: url('@/assets/svgs/login-bg.svg');
      background-position: center;
      background-repeat: no-repeat;
      content: '';
    }
  }
}
</style>