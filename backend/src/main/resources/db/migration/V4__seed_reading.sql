INSERT INTO reading_passage (title, content, difficulty) VALUES
('Urban green space and public health',
'Cities around the world are expanding rapidly, and this growth often comes at the expense of parks, gardens, and other green spaces. Urban planners used to treat such areas as optional decoration. Recent research, however, suggests that access to nature inside a city is closely linked to public health.

Several studies have found that people who live within a short walk of a park report lower stress and better sleep. Trees can also reduce summer temperatures by providing shade and by releasing moisture into the air. In neighbourhoods with few trees, heat-related hospital visits tend to rise during heatwaves. Green space may also encourage physical activity. A well-designed park gives residents a reason to walk, cycle, or play outdoors rather than remaining indoors.

Critics argue that land in city centres is too expensive to be used as parks. They prefer to build housing or commercial offices, which generate tax revenue. Supporters of green infrastructure reply that the long-term medical costs of heat, pollution, and inactivity may be even higher. Some cities have therefore adopted a mixed approach: they protect existing parks, plant trees along streets, and require new developments to include small gardens on roofs or in courtyards.

The evidence does not mean that parks can replace hospitals or public transport. It does suggest that urban nature is not a luxury. When cities grow, the quality of daily life depends not only on roads and buildings, but also on the presence of trees, water, and open ground.',
'easy'),
('The academic word list in higher education',
'University students are expected to read research papers, write essays, and take part in seminars. Success in these tasks depends partly on specialist knowledge, but it also depends on a shared academic vocabulary. In 2000, Averil Coxhead published the Academic Word List (AWL), a collection of word families that appear frequently across a wide range of university subjects, yet are not among the most common everyday English words.

The AWL was based on a corpus of academic texts from fields such as law, commerce, science, and the arts. Words like analyse, concept, and significant occur in many disciplines, so learning them can help a student who moves from one course to another. Teachers of English for academic purposes have used the list to design reading materials and vocabulary tests. Learners often find that they can guess the meaning of a new text more easily once they recognise these items.

The list is not perfect. Language changes, and new fields such as data science have introduced terms that were rare when the original corpus was compiled. Some researchers also argue that students need more than isolated words; they need phrases such as in contrast or it can be argued that. Nevertheless, the AWL remains a practical starting point. It offers a limited, high-value set of items that repay study, especially for students preparing for exams such as IELTS, where academic reading and writing are central.',
'medium'),
('Water scarcity and agricultural innovation',
'Fresh water is unevenly distributed across the planet, and agriculture uses the largest share of it. In many dry regions, farmers rely on rivers and underground aquifers that are being depleted faster than they can refill. Climate change may increase this pressure by making rainfall less predictable.

One response is to improve irrigation. Traditional flood irrigation wastes a large amount of water through evaporation and runoff. Drip systems deliver water slowly to the roots of plants, which can raise yields while using less water. Another approach is to grow crops that tolerate drought. Scientists have developed varieties of wheat and rice that survive longer dry periods, although these seeds may be expensive for small farms.

Policy also matters. If water is almost free, farmers have little reason to conserve it. Some governments have introduced prices or quotas, but these measures can be unpopular. There is also a trade-off between food security and environmental protection. Reducing irrigation may save rivers, yet it can lower harvests in the short term.

No single technology will solve water scarcity. Progress is more likely when better equipment, suitable crops, and fair rules are combined. For importing countries, diet also plays a role: foods that require large volumes of water, such as some types of meat, place extra demand on distant farms. Understanding this hidden water use is becoming part of the debate about sustainable food systems.',
'medium');

INSERT INTO reading_question (passage_id, question_type, question, options_json, answer, explanation) VALUES
(1, 'MULTIPLE_CHOICE', 'According to the passage, urban green space used to be seen as', '["a core part of public transport","optional decoration","a replacement for hospitals","the main source of tax revenue"]', 'optional decoration', '第一段提到规划者曾把绿地当作 optional decoration。'),
(1, 'TRUE_FALSE_NOT_GIVEN', 'People who live near parks always sleep longer than people who do not.', '["TRUE","FALSE","NOT GIVEN"]', 'FALSE', '文中说的是报告更好睡眠，并非 always sleep longer。'),
(1, 'MULTIPLE_CHOICE', 'Heat-related hospital visits are more likely in neighbourhoods with', '["many underground stations","few trees","large shopping malls","new roof gardens"]', 'few trees', '第二段明确提到少树社区在热浪期间就诊增加。'),
(1, 'TRUE_FALSE_NOT_GIVEN', 'All city governments now require roof gardens in new buildings.', '["TRUE","FALSE","NOT GIVEN"]', 'FALSE', '文中只说一些城市采取混合做法，并非所有政府。'),
(1, 'FILL_BLANK', 'Supporters of green infrastructure worry about long-term ______ costs.', '[]', 'medical', '第三段提到长期医疗成本可能更高。'),
(2, 'MULTIPLE_CHOICE', 'The Academic Word List was published in', '["1990","2000","2010","2020"]', '2000', '第一段写明 Coxhead 于 2000 年发表 AWL。'),
(2, 'MULTIPLE_CHOICE', 'The AWL focuses on words that are', '["the most common everyday English words","rare in every university subject","frequent across many academic fields","used only in data science"]', 'frequent across many academic fields', 'AWL 选的是跨学科高频、但不是最日常的词。'),
(2, 'TRUE_FALSE_NOT_GIVEN', 'The AWL was based on texts from only one academic subject.', '["TRUE","FALSE","NOT GIVEN"]', 'FALSE', '语料来自法律、商务、科学和艺术等多个领域。'),
(2, 'TRUE_FALSE_NOT_GIVEN', 'Some researchers think students also need academic phrases.', '["TRUE","FALSE","NOT GIVEN"]', 'TRUE', '第三段提到学生还需要短语。'),
(2, 'FILL_BLANK', 'The AWL remains a practical ______ point for learners.', '[]', 'starting', '最后一段说它仍是 practical starting point。'),
(3, 'MULTIPLE_CHOICE', 'According to the passage, agriculture', '["uses the smallest share of fresh water","uses the largest share of fresh water","does not depend on rivers","has solved water scarcity"]', 'uses the largest share of fresh water', '开篇指出农业占用最大份额淡水。'),
(3, 'MULTIPLE_CHOICE', 'Drip irrigation is described as a system that', '["floods entire fields quickly","delivers water slowly to plant roots","removes salt from seawater","makes rainfall more predictable"]', 'delivers water slowly to plant roots', '第二段对滴灌的定义。'),
(3, 'TRUE_FALSE_NOT_GIVEN', 'Drought-tolerant seeds are always cheap for small farms.', '["TRUE","FALSE","NOT GIVEN"]', 'FALSE', '文中说这些种子对小农场可能昂贵。'),
(3, 'TRUE_FALSE_NOT_GIVEN', 'Water pricing policies are always popular with farmers.', '["TRUE","FALSE","NOT GIVEN"]', 'FALSE', '文中说这些措施可能 unpopular。'),
(3, 'FILL_BLANK', 'No single ______ will solve water scarcity.', '[]', 'technology', '最后一段第一句。');
