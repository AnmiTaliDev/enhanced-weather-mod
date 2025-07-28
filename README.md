# Enhanced Weather Mod

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.2.0+-red.svg)](https://files.minecraftforge.net/)

Мод, добавляющий новые типы погоды в Minecraft Java Edition с улучшенными визуальными и звуковыми эффектами.

## Автор

**AnmiTaliDev** (anmitali198@gmail.com)
- GitHub: [AnmiTaliDev/enhanced-weather-mod](https://github.com/AnmiTaliDev/enhanced-weather-mod)

## Особенности

### Новые типы погоды

1. **🌪️ Буря (Storm)**
   - Усиленные молнии каждые 2 секунды
   - Частицы ветра и пыли
   - Звуковые эффекты ветра и далекого грома
   - Временное затемнение экрана
   - Замедление движения на открытом воздухе
   - Продолжительность: 5 минут

2. **🌧️ Слабый дождь (Light Rain)**
   - Уменьшенная интенсивность частиц дождя
   - Тихие звуки дождя
   - Медленное наполнение котлов водой
   - Ускоренный рост растений
   - Туманные эффекты для атмосферы
   - Продолжительность: 10 минут

3. **🌊 Паводок (Flood)**
   - Временное повышение уровня воды на 1-2 блока в низинах
   - Постепенный спад воды через 10-15 минут
   - Частицы брызг и водяного пара
   - Замедление движения в воде
   - Работает только в подходящих биомах (равнины, леса, болота)
   - Продолжительность: 15 минут

### Технические особенности

- ✅ Совместимость с ванильными биомами
- ✅ Плавные переходы между типами погоды
- ✅ Оптимизированная производительность
- ✅ Конфигурационные файлы для настройки
- ✅ Команды для администраторов

## Установка

### Системные требования

- **Minecraft**: 1.20.1
- **Minecraft Forge**: 47.2.0 или выше (Скоро будет Fabric версия)
- **Java**: 17 или выше

### Пошаговая установка

1. **Установите Minecraft Forge 1.20.1**
   - Скачайте установщик с [официального сайта Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)
   - Запустите установщик и выберите "Install client"

2. **Скачайте мод**
   - Перейдите на страницу [Releases](https://github.com/AnmiTaliDev/enhanced-weather-mod/releases)
   - Скачайте файл `enhanced-weather-mod-1.0.0.jar`

3. **Установите мод**
   - Откройте папку `.minecraft/mods/`
   - Поместите скачанный файл `.jar` в эту папку

4. **Запустите игру**
   - Выберите профиль Forge в лаунчере
   - Запустите игру

### Сборка из исходного кода

```bash
# Клонируйте репозиторий
git clone https://github.com/AnmiTaliDev/enhanced-weather-mod.git
cd enhanced-weather-mod

# Сборка мода (требуется JDK 17)
./gradlew build

# Найдите собранный мод в папке build/libs/
```

## Использование

### Команды

Все команды требуют права администратора (уровень операторов 2):

```
/enhancedweather [тип]          - Изменить погоду или показать информацию
/enhancedweather clear          - Ясная погода
/enhancedweather storm          - Буря
/enhancedweather lightrain      - Слабый дождь
/enhancedweather flood          - Паводок

# Краткие команды:
/estorm      - Начать бурю
/elightrain  - Начать слабый дождь
/eflood      - Начать паводок
/eclear      - Очистить погоду
```

### Примеры использования

```bash
# Проверить текущую погоду
/enhancedweather

# Запустить бурю на 5 минут
/enhancedweather storm

# Быстро очистить погоду
/eclear
```

### Автоматическая погода

Мод автоматически генерирует случайную погоду каждые ~20 минут игрового времени. Вероятности:
- Буря: 5%
- Слабый дождь: 8%
- Паводок: 3%

## Конфигурация

Файл конфигурации находится в `data/enhancedweather/weather_config.json`:

```json
{
  "weather_settings": {
    "storm": {
      "enabled": true,
      "duration_ticks": 6000,
      "spawn_chance": 0.05
    },
    "light_rain": {
      "enabled": true,
      "duration_ticks": 12000,
      "spawn_chance": 0.08
    },
    "flood": {
      "enabled": true,
      "duration_ticks": 18000,
      "spawn_chance": 0.03
    }
  }
}
```

### Основные настройки

- `enabled`: Включить/выключить тип погоды
- `duration_ticks`: Длительность в тиках (20 тиков = 1 секунда)
- `spawn_chance`: Вероятность появления (0.0 - 1.0)
- `lightning_frequency`: Частота молний для бури
- `particle_count`: Количество частиц

### Известные проблемы

- Моды, полностью переписывающие систему погоды, могут конфликтовать
- На слабых компьютерах может снижаться FPS во время бури

## Лицензия

Этот проект лицензирован под [Apache License 2.0](LICENSE).

## Поддержка

### Сообщить о проблеме

1. Проверьте [список известных проблем](https://github.com/AnmiTaliDev/enhanced-weather-mod/issues)
2. Создайте новую issue с подробным описанием
3. Приложите лог-файлы из папки `logs/`

### Предложить улучшение

1. Откройте issue с тегом "enhancement"
2. Опишите желаемую функциональность
3. Обоснуйте необходимость изменения

### Связь с автором

- Email: anmitali198@gmail.com
- GitHub: [@AnmiTaliDev](https://github.com/AnmiTaliDev)

## Благодарности

- Команде Minecraft Forge за отличную модификационную платформу
- Сообществу модостроителей за поддержку и вдохновение
- Всем тестерам и пользователям мода

---

**Создано с ❤️ для сообщества Minecraft**