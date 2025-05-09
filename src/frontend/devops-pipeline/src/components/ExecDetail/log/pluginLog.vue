<template>
    <section class="plugin-log">
        <bk-log-search
            :execute-count="executeCount"
            @change-execute="changeExecute"
            class="log-tools"
        >
            <template v-slot:tool>
                <li
                    class="more-button"
                    @click="toggleShowDebugLog"
                >
                    {{ showDebug ? $t("hideDebugLog") : $t("showDebugLog") }}
                </li>
                <li
                    class="more-button"
                    @click="downloadLog"
                >
                    {{ $t("downloadLog") }}
                </li>
            </template>
        </bk-log-search>
        <bk-log
            class="bk-log"
            ref="scroll"
            :enable-a-i="enableAI"
            :ai-tips="aiTips"
            @tag-change="tagChange"
            @praise-ai="handlePraiseAi"
            @down-praise-ai="handleDownPraiseAi"
            @load-ai-message="handleLoadAiMessage"
            @reload-ai-message="handleReloadAiMessage"
            @cancel-praise-ai="handleCancelPraiseAI"
            @cancel-down-praise-ai="handleCancelDownPraiseAI"
        />
    </section>
</template>

<script>
    import {
        RESOURCE_ACTION,
        handlePipelineNoPermission
    } from '@/utils/permission'
    import { hashID } from '@/utils/util.js'
    import { bkLog, bkLogSearch } from '@blueking/log'
    import { mapActions, mapState } from 'vuex'

    export default {
        components: {
            bkLogSearch,
            bkLog
        },

        props: {
            id: {
                type: String,
                default: undefined
            },
            type: {
                type: String,
                default: 'pluginLog'
            },
            buildId: {
                type: String
            },
            executeCount: {
                type: Number
            },
            execDetail: {
                type: Object,
                required: true
            }
        },

        data () {
            return {
                postData: {
                    projectId: this.$route.params.projectId,
                    pipelineId: this.$route.params.pipelineId,
                    buildId: this.buildId,
                    tag: this.type === 'pluginLog' ? this.id : undefined,
                    jobId: this.type === 'containerLog' ? this.id : undefined,
                    subTag: '',
                    currentExe: this.executeCount,
                    lineNo: 0,
                    debug: false
                },
                timeId: '',
                clearIds: [],
                showDebug: false,
                enableAI: false,
                aiTips: ''
            }
        },

        computed: {
            ...mapState('atom', ['editingElementPos']),

            downloadLink () {
                const editingElementPos = this.editingElementPos
                if (this.type === 'containerLog') {
                    const fileName = encodeURI(
                        encodeURI(
                            `${editingElementPos.stageIndex + 1}-${
                                editingElementPos.containerIndex + 1
                            }-${this.currentJob.name}`
                        )
                    )
                    const jobId = this.currentJob.containerHashId
                    return `${API_URL_PREFIX}/log/api/user/logs/${this.$route.params.projectId}/${this.$route.params.pipelineId}/${this.execDetail.id}/download?jobId=${jobId}&executeCount=${this.postData.currentExe}&fileName=${fileName}`
                } else {
                    const fileName = encodeURI(
                        encodeURI(
                            `${editingElementPos.stageIndex + 1}-${
                                editingElementPos.containerIndex + 1
                            }-${editingElementPos.elementIndex + 1}-${this.currentElement.name}`
                        )
                    )
                    const tag = this.currentElement.id
                    return `${API_URL_PREFIX}/log/api/user/logs/${this.$route.params.projectId}/${this.$route.params.pipelineId}/${this.execDetail.id}/download?tag=${tag}&executeCount=${this.postData.currentExe}&fileName=${fileName}`
                }
            },

            downloadAllLink () {
                const fileName = encodeURI(encodeURI(this.execDetail.pipelineName))
                return `${API_URL_PREFIX}/log/api/user/logs/${this.$route.params.projectId}/${this.$route.params.pipelineId}/${this.execDetail.id}/download?executeCount=1&fileName=${fileName}`
            },

            currentJob () {
                const { editingElementPos, execDetail } = this
                const model = execDetail.model || {}
                const stages = model.stages || []
                const currentStage = stages[editingElementPos.stageIndex] || []

                try {
                    if (editingElementPos.containerGroupIndex === undefined) {
                        return currentStage.containers[editingElementPos.containerIndex]
                    } else {
                        return currentStage.containers[editingElementPos.containerIndex]
                            .groupContainers[editingElementPos.containerGroupIndex]
                    }
                } catch (_) {
                    return {}
                }
            },

            currentElement () {
                const {
                    editingElementPos: { elementIndex },
                    currentJob
                } = this
                return currentJob.elements[elementIndex]
            }
        },

        mounted () {
            this.getLog()
            this.checkAIStatus()
        },

        beforeDestroy () {
            this.closeLog()
        },

        methods: {
            ...mapActions('atom', [
                'getInitLog',
                'getAfterLog',
                'getLogStatus',
                'getDownloadLogFromArtifactory',
                'praiseAi',
                'cancelPraiseAi',
                'getPraiseAiInfo',
                'getLogAIMessage',
                'getAIStatus'
            ]),

            checkAIStatus () {
                this.getAIStatus().then(res => {
                    this.enableAI = res.data
                    this.aiTips = this.$t('details.aiAnalysis', [this.$pipelineDocs.AIAnalysis])
                })
            },

            handlePraiseAi (item) {
                this.praiseAi({
                    ...this.postData,
                    score: true
                }).then(() => {
                    this.handleGetPraiseAiInfo(item)
                    this.$bkMessage({ theme: 'success', message: this.$t('successPraise') })
                })
            },

            handleDownPraiseAi (item) {
                this.praiseAi({
                    ...this.postData,
                    score: false
                }).then(() => {
                    this.handleGetPraiseAiInfo(item)
                    this.$bkMessage({ theme: 'success', message: this.$t('successDownPraise') })
                })
            },

            handleCancelPraiseAI (item) {
                this.cancelPraiseAi({
                    ...this.postData,
                    score: true
                }).then(() => {
                    this.handleGetPraiseAiInfo(item)
                    this.$bkMessage({ theme: 'success', message: this.$t('successCancelPraise') })
                })
            },

            handleCancelDownPraiseAI (item) {
                this.cancelPraiseAi({
                    ...this.postData,
                    score: false
                }).then(() => {
                    this.handleGetPraiseAiInfo(item)
                    this.$bkMessage({ theme: 'success', message: this.$t('successCancelDownPraise') })
                })
            },

            handleGetPraiseAiInfo (item) {
                const scrollRef = this.$refs.scroll
                this.getPraiseAiInfo({
                    ...this.postData
                })
                    .then((res) => {
                        item.goodUsers = res.data.goodUsers
                        item.badUsers = res.data.badUsers
                        scrollRef.setSingleLogData(item)
                    })
            },

            handleLoadAiMessage (item) {
                item.aiMessage = ''
                const scrollRef = this.$refs.scroll
                this.handleGetPraiseAiInfo(item)
                this.getLogAIMessage({
                    ...this.postData,
                    refresh: false,
                    callBack (val) {
                        item.aiMessage += val
                        scrollRef.setSingleLogData(item)
                        scrollRef.scrollAILogToBottom()
                    }
                })
            },

            handleReloadAiMessage (item) {
                item.aiMessage = ''
                const scrollRef = this.$refs.scroll
                this.getLogAIMessage({
                    ...this.postData,
                    refresh: true,
                    callBack (val) {
                        item.aiMessage += val
                        scrollRef.setSingleLogData(item)
                        scrollRef.scrollAILogToBottom()
                    }
                }).then(() => {
                    this.handleGetPraiseAiInfo(item)
                })
            },

            getLog () {
                const id = hashID()
                this.getLog.id = id
                let logMethod = this.getAfterLog
                if (this.postData.lineNo <= 0) logMethod = this.getInitLog

                logMethod(this.postData)
                    .then((res) => {
                        if (this.clearIds.includes(id)) return

                        const scroll = this.$refs.scroll
                        res = res.data || {}
                        if (res.status !== 0) {
                            const errMessage = res.message ?? this.$t('history.logErr')

                            scroll.handleApiErr(errMessage)
                            return
                        }

                        const logs = res.logs || []
                        const lastLog = logs[logs.length - 1] || {}
                        const lastLogNo = lastLog.lineNo || this.postData.lineNo - 1 || -1
                        this.postData.lineNo = +lastLogNo + 1

                        const subTags = res.subTags
                        if (subTags && subTags.length > 0) {
                            const tags = subTags.map((tag) => ({ label: tag, value: tag }))
                            tags.unshift({ label: 'ALL', value: '' })
                            scroll.setSubTag(tags)
                        }

                        if (res.finished) {
                            if (res.hasMore) {
                                scroll.addLogData(logs)
                                this.timeId = setTimeout(this.getLog, 100)
                            } else {
                                scroll.addLogData(logs)
                            }
                        } else {
                            scroll.addLogData(logs)
                            this.timeId = setTimeout(this.getLog, 1000)
                        }
                    })
                    .catch((err) => {
                        if (err.status === 403) {
                            handlePipelineNoPermission({
                                projectId: this.$route.params.projectId,
                                resourceCode: this.$route.params.pipelineId,
                                action: RESOURCE_ACTION.EXECUTE
                            })
                        }
                        this.$bkMessage({ theme: 'error', message: err.message || err })
                        this.handleApiErr(err.message)
                    })
            },

            tagChange (val) {
                this.postData.subTag = val
                this.postData.lineNo = 0
                this.closeLog()
                this.getLog()
            },

            changeExecute (execute) {
                this.postData.currentExe = execute
                this.postData.lineNo = 0
                this.closeLog()
                this.getLog()
            },

            closeLog () {
                clearTimeout(this.timeId)
                this.clearIds.push(this.getLog.id)
            },

            handleApiErr (err) {
                const scroll = this.$refs.scroll
                if (scroll) scroll.handleApiErr(err)
            },

            toggleShowDebugLog () {
                this.showDebug = !this.showDebug
                this.$refs.scroll.changeExecute()
                this.postData.debug = this.showDebug
                this.postData.lineNo = 0
                this.closeLog()
                this.getLog()
            },

            async downloadLog () {
                const isPluginLog = this.id !== undefined
                let downloadLink = this.downloadAllLink

                if (isPluginLog) {
                    const pluginData = {
                        projectId: this.$route.params.projectId,
                        pipelineId: this.$route.params.pipelineId,
                        buildId: this.buildId,
                        tag: this.id,
                        executeCount: this.postData.currentExe
                    }
                    const logStatusRes = await this.getLogStatus(pluginData)
                    const data = logStatusRes.data || {}
                    const logMode = data.logMode || ''
                    downloadLink
                        = logMode === 'ARCHIVED'
                            ? await this.getDownloadLogFromArtifactory(pluginData)
                            : this.downloadLink
                    if (logMode === 'LOCAL') {
                        this.$bkMessage({ theme: 'primary', message: this.$t('history.uploadLog'), limit: 1 })
                        return
                    }
                }
                location.href = downloadLink
            }
        }
    }
</script>

<style lang="scss" scoped>
.plugin-log {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  overflow-y: auto;
  .log-tools {
    position: absolute;
    right: 20px;
    top: 13px;
    display: flex;
    align-items: center;
    line-height: 30px;
    user-select: none;
    background: none;
  }
}
</style>
