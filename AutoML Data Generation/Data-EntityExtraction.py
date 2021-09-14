# This is a sample Python script.
# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.


normal_app_names = [
    "UberEats",
    "Swiggy",
    "Zomato",
    'Slack',
    'Google',
    'COVIDSafe',
    'ZOOM',
    'Smart Launcher',
    'TikTok',
    'Houseparty',
    'Instagram',
    'Coronavirus Australia',
    'Disney',
    'WhatsApp Messenger',
    'YouTube',
    'Spotify',
    'Snapchat',
    'Facebook',
    'Doordash',
    'Netflix',
    'Gmail',
    'Widgetsmith',
    'Procreate Pocket',
    'TouchRetouch',
    'Rain Parrot',
    'Facetune',
    'SkyView',
    'MusicView',
    'Shadowrocket',
    "Slack",
    'LastPass',
    'Grammarly',
    'Dropbox',
    'Google Drive',
    'OfficeSuite',
    'Imgur',
    'KineMaster',
]

faulty_app_names = [
    'musics player',
    'musical player',
    'store',

]

unnecessary_word_app_names = [
    'Facebook Messenger',
    'Asana project management app',
    'Google Play Store',
    'Google Maps',
    'Google Gmail'
]

multi_word_apps = [
    'WikiCamps Australia',
    'The Wonder Weeks',
    'Sezzy Timer',
    'Adobe Photoshop Express',
    'Adobe Lightroom',
    'mymacca’s Ordering & Offers',
    'Microsoft Teams',
    'TripView - Sydney & Melbourne',
    'AutoSleep Track Sleep on Watch',
]

click_actions = [
    'click', 'tap', 'touch'
]

open_actions = [
    'open', 'launch', 'start'
]

scroll_actions = [
    'scroll up',
    'swipe up',
    'scroll down',
    'swipe down',
    'scroll left',
    'swipe left',
    'scroll right',
    'swipe right',
]

enter_actions = [
    'enter', 'search', 'find', 'look up',
]
locations = [
    'Fish and chips',
    'Sandwich',
    'Hamburger',
    'Chicken nugget',
    'Pizza',
    'Hot dog',
    'Ice cream',
    'Breakfast burrito',
    'Hot chocolate',
    'Tomato',
    'Croissant',
    'Pancake',
    'sandwich',
    'Coffee',
    'Fried chicken',
    'Pizza',
    'Kebab',
    'bar',
    'bank',
    'bakery',
    'airport',
    'bookstore ',
    'station',
    'church',
    'court',
    'department store',
    'cinema ',
    'gym',
    'hospital',
    'hotel',
    'zoo',
    'supermarket',
    'school',
    'restaurant',
    'park',
    'motel',
    'pharmacy',
    'museum',
    'mall',
    'library',
    'library',
    'motel',
    'ATM',
    'kindergarten',
]

all_apps = [*normal_app_names, *multi_word_apps, *faulty_app_names, *unnecessary_word_app_names]

articles = [
    'a',
    'an',
    'the'
]

fillers = [
    'Hey',
    'I',
    'Like',
    'You',
    'Ignore',
    'Can',
    'But',
    'About',
    'The',
    'So',
    'Please',
    'We',

]

item = [
    'clothes',
    'grocery',
    'food',
    'shoes',
    'cosmetics',
    'equipment',
    'electronics',
    'household appliance',
    'smartphone',
    'juice'
]


def data_create():

    outputStr = []
    index = 0
    for app in normal_app_names:
        for word2 in open_actions:
            for word3 in item:
                string = word2 + " " + word3 + " from " + app
                if index//22 == 0:
                    string += string + "s"

                elif index//24 == 0:
                    string += string + "es"

                if index//27 == 0:
                    string = word2 + " " + word3 + " from the " + app

                elif index//26 == 0:
                    string = word2 + " " + word3 + " from a " + app
                index += 1
                outputStr.append(string)
        for word2 in open_actions:
            for word4 in item:
                string = word2 + " " + app + ' and then ' + word2 + ' down and search ' + word4
                outputStr.append(string)

    # open (App name) app
    counter = 0
    for open_word in open_actions:
        for app in all_apps:
            if counter % 5 == 0:
                string = open_word + " the " + app
            elif counter % 7 == 0:
                string =   open_word + " a " + app
            else:
                string = open_word + " " + app
            if counter % 20 == 0:
                string += " app"
            counter = counter + 1
            outputStr.append(string)
    print(outputStr)

    # search something in app
    counter = 0
    for enter_word in enter_actions:
        for app in all_apps:
            for location in locations:
                if counter % 5 == 0:
                    string = enter_word + " " + location + " the " + app
                elif counter % 7 == 0:
                    string = enter_word + " " + location + " a " + app
                else:
                    string = enter_word + " " + location + app
                if counter % 20 == 0:
                    string += " app"
                counter = counter + 1
                outputStr.append(string)
    print(outputStr)

    return outputStr


def entity_analysis(content, doc):

    content = content.strip()
    data = clean_data(content)
    finalOutput = '{"annotations":['
    index = 0

    if len(data) != 0:
        for word in data:

            output1 = '{"text_extraction":{"text_segment":{"end_offset":' + str(word[2]) + \
                      ',"start_offset":' + str(word[1]) + '}},"display_name":' + '"' + word[0] + '"' + '}'

            if index != 0:
                finalOutput = finalOutput + ',' + output1
            else:
                finalOutput += output1
            index += 1

        output3 = '],"text_snippet":{"content":' + '"' + content + '"' + '}}\n'
        finalOutput += output3

    else:
        print("No content!")

    value = str(finalOutput)
    doc.write(str(value))


def clean_data(unclean_data):

    # unclean_data = joinApp(unclean_data)
    unclean_data = unclean_data.split(' ')
    end_offset = 0
    data = []

    for word in unclean_data:

        entity = ''

        if end_offset == 0:
            start_offset = 0
        else:
            start_offset = end_offset + 2
        end_offset = start_offset + len(word)

        word = word.lower()
        check = False
        for word0 in fillers:
            word0 = word0.lower()
            if word == word0:
                check = True
                break

        if check:
            continue

        check1 = False
        for word1 in applications:
            word1 = word1.lower()
            if word == word1:
                entity = "Applications"
                check1 = True

        check2 = False
        if entity == '':
            for word2 in foods:
                word2 = word2.lower()
                if word == word2:
                    entity = "Foods"
                    check2 = True

        check3 = False
        if entity == '':
            for word3 in actions:
                word3 = word3.lower()
                if word == word3:
                    entity = "Actions"
                    check3 = True

        # check4 = False
        # if entity == '':
        #     for word4 in food_application:
        #         word4 = word4.lower()
        #         if word == word4:
        #             entity = "Food Application"
        #             check4 = True

        if not (check1 or check2 or check3):
            continue

        data.append((entity, start_offset, end_offset))

    return data


# def joinApp(content):
#
#     end_offset = 0
#     mark = 0
#
#     content_list = content.split(' ')
#     for word in content_list:
#         word = word.lower()
#
#         if end_offset == 0:
#             start_offset = 0
#         else:
#             start_offset = end_offset + 2
#         end_offset = start_offset + len(word) - 1
#
#         for word3 in actions:
#             word3 = word3.lower()
#             if word == word3:
#                 mark = end_offset + 1
#                 break
#
#     return content


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    dataset = data_create()
    # test = "Hey I like android development so can you please launch UberEATS we google about the proposal"
    file = open('label_data.jsonl', 'w', encoding='utf-8')
    for commands in dataset:
        entity_analysis(commands, file)
    file.close()
