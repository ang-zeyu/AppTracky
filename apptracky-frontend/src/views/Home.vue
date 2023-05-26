<template>
  <v-container fluid>
    <v-row>
      <v-col>
        <v-sheet class="d-flex justify-space-between align-center pa-3" rounded="lg">
          <div class="d-flex align-center">
            <v-icon icon="mdi-send" color="light-blue"></v-icon>
            <h2 class="ms-2 text-h4 d-inline font-weight-bold">Applications</h2>
          </div>
          <v-btn
            icon="mdi-plus-thick"
            ripple
            variant="tonal"
            @click="openAddForm"
          ></v-btn>
        </v-sheet>
      </v-col>
    </v-row>
    <v-row class="justify-end">
      <v-col
        v-for="stage in stages"
        :key="stage.value"
        sm="6"
        md="2"
      >
        <v-card
          :subtitle="stage.displayName"
          min-height="500px"
          rounded="lg"
          :data-stage="stage.value"
          @dragenter="colOnDragEnter"
          @dragover="colOnDragOver"
          :color="selectedCol === stage.value ? 'grey-lighten-4' : 'white'"
        >
          <v-card
            v-for="application in applications.filter(app => app.applicationStage === stage.value)"
            :key="application.id"
            :title="application.title"
            :subtitle="application.company"
            :text="application.recruiter"
            :href="application.url"
            class="ma-2 apt-application-card"
            color="grey-lighten-5"
            elevation="2"
            draggable="true"
            @dragstart="cardOnDragStart"
            @dragend="cardOnDragEnd(application)"
          >
            <template #append>
              <div class="d-flex flex-column">
                <v-btn
                  size="x-small"
                  icon="mdi-pencil"
                  rounded
                  class="apt-application-card-action"
                  @click="openEditForm($event, application)"
                >
                </v-btn>
                <v-btn
                  size="x-small"
                  icon="mdi-delete"
                  color="red"
                  rounded
                  class="mt-1 apt-application-card-action"
                  @click="deleteApplication($event, application)"
                >
                </v-btn>
                <v-btn
                  v-if="application.hasResume"
                  size="x-small"
                  icon="mdi-file"
                  color="grey"
                  rounded
                  class="mt-1 apt-application-card-action"
                  @click="openResumeUrl($event, application.id)"
                >
                </v-btn>
              </div>
            </template>
          </v-card>
        </v-card>
      </v-col>
    </v-row>
    <v-overlay v-model="formState.show" class="d-flex justify-center align-center">
      <v-card :min-width="400">
        <v-card-title>{{ formState.id ? 'Edit Application' : 'Add Application' }}</v-card-title>
        <v-card-item>
          <v-form @submit.prevent="submitForm">
            <v-text-field
              label="Title"
              prepend-inner-icon="mdi-format-title"
              v-model="formState.title"
              :rules="validators.title"
            ></v-text-field>
            <v-text-field
              label="URL"
              prepend-inner-icon="mdi-web"
              v-model="formState.url"
              :rules="validators.url"
            ></v-text-field>
            <v-text-field
              label="Company"
              prepend-inner-icon="mdi-domain"
              v-model="formState.company"
              :rules="validators.company"
            ></v-text-field>
            <v-text-field
              label="Recruiter"
              prepend-inner-icon="mdi-account"
              v-model="formState.recruiter"
            ></v-text-field>
            <v-select
              label="Stage"
              :items="stages"
              item-title="displayName"
              item-value="value"
              prepend-inner-icon="mdi-progress-helper"
              v-model="formState.applicationStage"
              :rules="validators.stage"
            ></v-select>
            <v-file-input
              accept=".pdf"
              label="Resume"
              prepend-icon=""
              prepend-inner-icon="mdi-paperclip"
              name="resume"
            >
            </v-file-input>
            <v-btn type="submit" block color="blue-lighten-1" class="mt-3" ripple>
              {{ formState.id ? 'Update' : 'Submit' }}
            </v-btn>
          </v-form>
        </v-card-item>
      </v-card>
    </v-overlay>
  </v-container>
</template>

<script lang="ts" setup>
import { getCustom, postCustom, putCustom, delCustom } from '@/utils/fetch';
import { Ref, reactive } from 'vue';
import { ref } from 'vue';
import { SubmitEventPromise } from 'vuetify/lib/framework.mjs';

interface Application {
  id: number,
  title: string,
  url: string,
  company: string,
  recruiter?: string,
  applicationStage: string,
  hasResume: boolean,
  resumeGetUrl?: string,
  resumePutUrl?: string,
}

// ---------------------------------------------------
// Drag and drop
const selectedCol: Ref<string | undefined> = ref();

function cardOnDragStart(ev: DragEvent) {
  selectedCol.value = undefined;
  (ev.dataTransfer as DataTransfer).effectAllowed = 'move';
}

async function cardOnDragEnd(application: Application) {
  const targetStage = selectedCol.value;
  selectedCol.value = undefined;
  if (!targetStage || targetStage === application.applicationStage) {
    return;
  }

  try {
    application.applicationStage = targetStage;
    await putCustom('api/applications', application);
  } catch (ex) {
    // TODO show alert popup
  }
  await refreshApplications();
}

function colOnDragEnter(ev: DragEvent) {
  ev.preventDefault();
  (ev.dataTransfer as DataTransfer).dropEffect = 'move';
  selectedCol.value = (ev.target as HTMLElement).dataset.stage;
  console.log(selectedCol);
}

function colOnDragOver(ev: DragEvent) {
  ev.preventDefault();
  (ev.dataTransfer as DataTransfer).dropEffect = 'move';
}
// ---------------------------------------------------

// ---------------------------------------------------
// Constants

const initialFormState = {
  id: null,
  title: '',
  url: '',
  company: '',
  recruiter: null,
  applicationStage: null,
  show: false
};

const stages = [
  { displayName: 'Saved', value: 'SAVED' },
  { displayName: 'Applied', value: 'APPLIED' },
  { displayName: 'Interviewing', value: 'INTERVIEWING' },
  { displayName: 'Offer', value: 'OFFER' },
  { displayName: 'Hired', value: 'HIRED' },
  { displayName: 'Rejected', value: 'REJECTED' },
];

const validators = {
  title: [
    (v?: string) => (v && v.length > 2) || 'A job title is required'
  ],
  url: [
    (v?: string) => (v && /^(http(s):\/\/.)[-a-zA-Z0-9@:%._+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_+.~#?&//=]*)$/.test(v))
      || 'Invalid URL'
  ],
  company: [
    (v?: string) => (v && v.length > 2) || 'A company is required'
  ],
  stage: [
    (v?: string) => (v && stages.some(s => s.value === v)) || 'Please select where you are at in the application.'
  ],
}

// ---------------------------------------------------

// ---------------------------------------------------
// Data

const applications: Ref<Application[]> = ref([]);
const formState: any = reactive({ ...initialFormState });

// ---------------------------------------------------

// ---------------------------------------------------
// Methods

async function openResumeUrl(ev: MouseEvent, id: number) {
  ev.preventDefault();
  ev.stopPropagation();

  const application = await (await getCustom(`api/applications/${id}?includeResume=true`)).json() as Application;
  if (!application.resumeGetUrl) {
    await refreshApplications();
    // TODO error
    return;
  }

  open(application.resumeGetUrl, '_blank');
}

async function refreshApplications() {
  const resp = await getCustom('api/applications');
  const apps = await resp.json() as Application[];
  applications.value = apps;
}

function openAddForm() {
  Object.assign(formState, initialFormState);
  formState.show = true;
}

async function openEditForm(ev: MouseEvent, application: Application) {
  ev.preventDefault();
  ev.stopPropagation();
  formState.id = application.id;
  formState.title = application.title;
  formState.url = application.url;
  formState.company = application.company;
  formState.recruiter = application.recruiter;
  formState.applicationStage = application.applicationStage;
  formState.show = true;
}

async function deleteApplication(ev: MouseEvent, application: Application) {
  ev.preventDefault();
  ev.stopPropagation();
  
  try {
    await delCustom('api/applications', application);
  } catch (ex) {
    // TODO show alert popup
  }
  await refreshApplications();
}

async function submitForm(ev: SubmitEventPromise) {
  const validationResult = await ev;
  if (!validationResult.valid) {
    console.log(formState.valid);
    return;
  }

  const isEdit = !!formState.id;

  const fileInputEl = (ev.target as HTMLFormElement).elements.namedItem('resume') as HTMLInputElement;
  const fileList = fileInputEl.files;
  const file = fileList && fileList.length && fileList[0];

  const savedApplicationResp = isEdit
    ? await putCustom('api/applications', { ...formState, hasResume: !!file })
    : await postCustom('api/applications', { ...formState, hasResume: !!file });

  async function revert() {
    if (isEdit) {
      await putCustom('api/applications', { ...applications.value.find(app => app.id === formState.id) });
    } else {
      await delCustom('api/applications', { ...savedApplicationResp });
    }
    await refreshApplications();

    // TODO UI error instead and return
    throw new Error('s3PutUrl does not exist');
  }

  if (file) {
    const savedApplication = await savedApplicationResp.json() as Application;
    const s3PutUrl = savedApplication.resumePutUrl;
    if (!s3PutUrl) {
      // Revert, delete the application or re-edit it
      await revert();
      return;
    }

    const formData = new FormData();
    formData.append('Content-Type', file.type);
    formData.append('file', file);

    try {
      await fetch(s3PutUrl, {
        method: 'PUT',
        body: formData
      });
    } catch (ex) {
      await revert();
      throw ex;
    }
  }

  // TODO error, loading states

  formState.show = false;
  await refreshApplications();
}
// ---------------------------------------------------

// ---------------------------------------------------
// Initialisation

refreshApplications();
</script>

<style>
.apt-application-card .v-card-item {
  padding-left: 0.5rem;
  padding-right: 0.5rem;
}

.apt-application-card .apt-application-card-action {
  opacity: 0;
  transition: opacity 0.3s linear;
}

.apt-application-card:hover .apt-application-card-action {
  opacity: 1;
}
</style>
