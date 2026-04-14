export const categories = ['证件', '电子产品', '书籍资料', '钥匙卡证', '衣物配饰', '其他']

export const notices = [
  {
    id: 1,
    title: '值班室认领提醒',
    summary: '工作日 9:00 - 17:30 可携带有效证明前往校团委值班室办理认领。',
    level: 'pending',
    publishedAt: '2026-04-10',
  },
  {
    id: 2,
    title: '证件类物品优先登记',
    summary: '身份证、学生证、银行卡类物品将在审核通过后优先展示与通知。',
    level: 'urgent',
    publishedAt: '2026-04-08',
  },
]

export const lostItems = [
  {
    id: 101,
    type: 'lost',
    title: '黑色双肩包丢失',
    itemName: '双肩包',
    category: '书籍资料',
    location: '图书馆二层自习区',
    time: '2026-04-11 15:30',
    description: '包内有数据库原理教材、银色保温杯和校园卡套。',
    contact: '188****1024',
    owner: '王同学',
    status: 'urgent',
  },
  {
    id: 102,
    type: 'lost',
    title: 'AirPods 充电盒丢失',
    itemName: 'AirPods 充电盒',
    category: '电子产品',
    location: '食堂一楼',
    time: '2026-04-10 12:20',
    description: '透明保护套，盒盖内侧贴有蓝色字母贴纸。',
    contact: '136****9812',
    owner: '赵同学',
    status: 'pending',
  },
]

export const foundItems = [
  {
    id: 201,
    type: 'found',
    title: '教学楼捡到学生证',
    itemName: '学生证',
    category: '证件',
    location: 'A3 教学楼 301 门口',
    time: '2026-04-12 08:45',
    description: '证件套为透明磨砂款，内含一卡通。',
    contact: '校青协值班号',
    owner: '刘老师',
    status: 'pending',
  },
  {
    id: 202,
    type: 'found',
    title: '操场看台拾到车钥匙',
    itemName: '车钥匙',
    category: '钥匙卡证',
    location: '操场东侧看台',
    time: '2026-04-11 18:05',
    description: '黑色钥匙扣，挂有小熊玩偶。',
    contact: '137****2213',
    owner: '陈同学',
    status: 'claimed',
  },
]

export const claims = [
  {
    id: 301,
    itemTitle: '教学楼捡到学生证',
    stage: '待审核',
    proof: '补充了证件号码后四位和学院信息',
    updatedAt: '2026-04-12 11:20',
  },
  {
    id: 302,
    itemTitle: '操场看台拾到车钥匙',
    stage: '已通过',
    proof: '描述了钥匙扣上的小熊挂件',
    updatedAt: '2026-04-11 21:10',
  },
]

export const dashboardStats = [
  { label: '待审核失物', value: 8, hint: '发布后 15 分钟内进入管理员工作台' },
  { label: '待审核招领', value: 5, hint: '证件类优先人工确认' },
  { label: '待审核认领', value: 3, hint: '需核对物品特征说明' },
  { label: '已完成认领', value: 26, hint: '本周累计成功找回' },
]

export const trend = [
  { day: '04-06', posts: 6 },
  { day: '04-07', posts: 9 },
  { day: '04-08', posts: 7 },
  { day: '04-09', posts: 12 },
  { day: '04-10', posts: 8 },
  { day: '04-11', posts: 14 },
  { day: '04-12', posts: 10 },
]

export const reviewQueue = [
  { id: 'L-208', title: '图书馆丢失笔记本电脑', owner: '赵同学', stage: '失物待审', priority: 'urgent' },
  { id: 'F-116', title: '食堂捡到校园卡', owner: '后勤值班', stage: '招领待审', priority: 'pending' },
  { id: 'C-046', title: '学生证认领申请', owner: '李同学', stage: '认领待审', priority: 'claimed' },
]

export const reportQueue = [
  { id: 'R-18', title: '重复发布疑似广告内容', status: '待处理', source: 'found_item#41' },
  { id: 'R-19', title: '联系方式疑似无效', status: '待处理', source: 'lost_item#66' },
]
