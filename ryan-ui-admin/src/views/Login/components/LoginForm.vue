<template>
  <el-form
    :model="loginData.loginForm"
    :rules="LoginRules"
    label-position="top"
    size="large"
    label-width="auto"
    v-show="getShow"
    ref="formLogin"
  >
    <el-row style="maring-left: -10px; maring-right: -10px">
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <LoginFormTitle style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="tenantName">
          <el-input
            type="text"
            v-model="loginData.loginForm.tenantName"
            :placeholder="t('login.tenantNamePlaceholder')"
            :prefix-icon="iconHouse"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="username">
          <el-input
            type="text"
            v-model="loginData.loginForm.username"
            :placeholder="t('login.usernamePlaceholder')"
            :prefix-icon="iconAvatar"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="password">
          <el-input
            type="password"
            v-model="loginData.loginForm.password"
            show-password
            :placeholder="t('login.passwordPlaceholder')"
            :prefix-icon="iconLock"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <el-checkbox label="记住我" />
        </el-form-item>
      </el-col>
      <el-col :span="12" style="padding-left: 10px; padding-right: 10px" class="flex justify-end">
        <el-form-item>
          <el-link type="primary" style="float: right">{{ t('login.forgetPassword') }}</el-link>
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item>
          <XButton
            :loading="loginLoading"
            type="primary"
            class="w-[100%]"
            :title="t('login.login')"
            @click="getCode()"
          />
        </el-form-item>
      </el-col>
      <Verify
        ref="verify"
        mode="pop"
        :captchaType="captchaType"
        :imgSize="{ width: '400px', height: '200px' }"
        @success="handleLogin"
      />
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <el-row justify="space-between" style="width: 100%" :gutter="5">
            <el-col :span="8">
              <XButton
                class="w-[100%]"
                :title="t('login.btnMobile')"
                @click="setLoginState(LoginStateEnum.MOBILE)"
              />
            </el-col>
            <el-col :span="8">
              <XButton
                class="w-[100%]"
                :title="t('login.btnQRCode')"
                @click="setLoginState(LoginStateEnum.QR_CODE)"
              />
            </el-col>
            <el-col :span="8">
              <XButton
                class="w-[100%]"
                :title="t('login.btnRegister')"
                @click="setLoginState(LoginStateEnum.REGISTER)"
              />
            </el-col>
          </el-row>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { ElLoading } from 'element-plus'
import LoginFormTitle from './LoginFormTitle.vue'
import { useIcon } from '@/hooks/web/useIcon'
import { LoginStateEnum, useLoginState, useFormValid } from './useLogin'
import * as authUtil from '@/utils/auth'
import * as LoginApi from '@/api/login'
import { usePermissionStore } from '@/store/modules/permission'
import type { RouteLocationNormalizedLoaded } from 'vue-router'

const formLogin = ref()
const captchaType = ref('blockPuzzle') //
const { validForm } = useFormValid(formLogin)
const { currentRoute, push } = useRouter()
const { setLoginState, getLoginState } = useLoginState()
const iconHouse = useIcon({ icon: 'ep:house' })
const iconAvatar = useIcon({ icon: 'ep:avatar' })
const iconLock = useIcon({ icon: 'ep:lock' })
const { t } = useI18n()
const loginLoading = ref(false)
const redirect = ref<string>('')
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN)
const permissionStore = usePermissionStore()
const verify = ref()
const LoginRules = {
  tenantName: [required],
  username: [required],
  password: [required]
}
const loginData = reactive({
  isShowPassword: false,
  captchaENale: import.meta.env.VITE_APP_CAPTCHA_ENABLE,
  tenantEnable: import.meta.env.VITE_APP_TENANT_ENABLE,
  loginForm: {
    tenantName: '芋道源码',
    username: 'admin',
    password: 'admin123',
    captchaVerification: '',
    rememberMe: false
  }
})
const getCookie = () => {
  const loginForm = authUtil.getLoginForm()
  if (loginForm) {
    loginData.loginForm = {
      ...loginData.loginForm,
      username: loginForm.username ? loginForm.username : loginData.loginForm.username,
      password: loginForm.password ? loginForm.password : loginData.loginForm.password,
      rememberMe: loginForm.rememberMe ? true : false,
      tenantName: loginForm.tenantName ? loginForm.tenantName : loginData.loginForm.tenantName
    }
  }
}
watch(
  () => currentRoute.value,
  (route: RouteLocationNormalizedLoaded) => {
    redirect.value = route?.query?.redirect as string
  },
  {
    immediate: true
  }
)
onMounted(() => {
  getCookie()
})

const getCode = async () => {
  if (loginData.captchaENale === 'false') {
    await handleLogin({})
  } else {
    verify.value.show()
  }
}
const handleLogin = async (params) => {
  loginLoading.value = true
  try {
    await getTenantId()
    const data = await validForm()
    if (!data) {
      return
    }
    loginData.loginForm.captchaVerification = params.captchaVerification
    const res = await LoginApi.loginApi(loginData.loginForm)
    if (!res) {
      return
    }
    ElLoading.service({
      lock: true,
      text: '正在加载系统中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    if (loginData.loginForm.rememberMe) {
      authUtil.setLoginForm(loginData.loginForm)
    } else {
      authUtil.removeLoginForm()
    }
    authUtil.setToken(res)
    if (!redirect.value) {
      redirect.value = '/'
    }
    push({ path: redirect.value || permissionStore.addRouters[0].path })
  } catch {
    loginLoading.value = false
  } finally {
    setTimeout(() => {
      const loadingInstance = ElLoading.service()
      loadingInstance.close()
    }, 400)
  }
}

const getTenantId = async () => {
  if (loginData.tenantEnable === 'true') {
    const res = await LoginApi.getTenantIdByNameApi(loginData.loginForm.tenantName)
    authUtil.setTenantId(res)
  }
}
</script>
<style scoped></style>
