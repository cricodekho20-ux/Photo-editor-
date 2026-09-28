package com.example.util

data class StickerCategory(
    val id: String,
    val name: String,
    val icon: String,
    val stickers: List<StickerItem>
)

data class StickerItem(
    val id: String,
    val symbol: String,
    val name: String,
    val defaultColor: Long = 0xFFFFFFFF
)

object StickerCatalog {
    val categories: List<StickerCategory> = listOf(
        StickerCategory(
            id = "emoji",
            name = "Emoji",
            icon = "😀",
            stickers = listOf(
                StickerItem("e1", "🔥", "Fire"),
                StickerItem("e2", "✨", "Sparkles"),
                StickerItem("e3", "⚡", "Lightning"),
                StickerItem("e4", "💥", "Boom"),
                StickerItem("e5", "⭐", "Star"),
                StickerItem("e6", "❤️", "Heart"),
                StickerItem("e7", "😍", "Love Eyes"),
                StickerItem("e8", "🚀", "Rocket"),
                StickerItem("e9", "👑", "Crown"),
                StickerItem("e10", "💎", "Diamond"),
                StickerItem("e11", "🎉", "Party"),
                StickerItem("e12", "🎯", "Target"),
                StickerItem("e13", "💯", "100"),
                StickerItem("e14", "👍", "Thumbs Up"),
                StickerItem("e15", "🤩", "Star Eyes"),
                StickerItem("e16", "😎", "Cool"),
                StickerItem("e17", "🎨", "Art Palette"),
                StickerItem("e18", "💡", "Idea Bulb")
            )
        ),
        StickerCategory(
            id = "youtube",
            name = "YouTube",
            icon = "▶️",
            stickers = listOf(
                StickerItem("yt1", "🔴 LIVE", "Live Badge", 0xFFEF4444),
                StickerItem("yt2", "🔔 SUBSCRIBE", "Subscribe", 0xFFEF4444),
                StickerItem("yt3", "👍 LIKE", "Like", 0xFF3B82F6),
                StickerItem("yt4", "4K ULTRA HD", "4K Badge", 0xFFF59E0B),
                StickerItem("yt5", "▶️ PLAY", "Play Button", 0xFFEF4444),
                StickerItem("yt6", "NEW VIDEO", "New Video", 0xFF10B981),
                StickerItem("yt7", "WATCH NOW", "Watch Now", 0xFF8B5CF6),
                StickerItem("yt8", "🚨 BREAKING", "Breaking", 0xFFDC2626),
                StickerItem("yt9", "MUST WATCH!", "Must Watch", 0xFFE11D48)
            )
        ),
        StickerCategory(
            id = "social",
            name = "Social",
            icon = "📱",
            stickers = listOf(
                StickerItem("s1", "📸 Instagram", "Instagram", 0xFFE1306C),
                StickerItem("s2", "💬 WhatsApp", "WhatsApp", 0xFF25D366),
                StickerItem("s3", "📘 Facebook", "Facebook", 0xFF1877F2),
                StickerItem("s4", "🐦 X/Twitter", "Twitter", 0xFF000000),
                StickerItem("s5", "✈️ Telegram", "Telegram", 0xFF229ED9),
                StickerItem("s6", "🔗 Link in Bio", "Link in Bio", 0xFF6366F1),
                StickerItem("s7", "📢 Follow Us", "Follow Us", 0xFF8B5CF6),
                StickerItem("s8", "🔄 Share", "Share", 0xFF06B6D4)
            )
        ),
        StickerCategory(
            id = "business",
            name = "Business",
            icon = "🏷️",
            stickers = listOf(
                StickerItem("b1", "50% OFF", "50% Off", 0xFFEF4444),
                StickerItem("b2", "BIG SALE", "Big Sale", 0xFFF59E0B),
                StickerItem("b3", "SPECIAL OFFER", "Special Offer", 0xFF10B981),
                StickerItem("b4", "BEST SELLER", "Best Seller", 0xFF8B5CF6),
                StickerItem("b5", "HOT DEAL 🔥", "Hot Deal", 0xFFF97316),
                StickerItem("b6", "LIMITED TIME", "Limited Time", 0xFFDC2626),
                StickerItem("b7", "NEW ARRIVAL", "New Arrival", 0xFF06B6D4),
                StickerItem("b8", "VERIFIED ✔️", "Verified", 0xFF3B82F6),
                StickerItem("b9", "FREE DELIVERY", "Free Delivery", 0xFF10B981)
            )
        ),
        StickerCategory(
            id = "festival",
            name = "Festival",
            icon = "🪔",
            stickers = listOf(
                StickerItem("f1", "🪔", "Diya"),
                StickerItem("f2", "🎆", "Fireworks"),
                StickerItem("f3", "🎇", "Sparkler"),
                StickerItem("f4", "🌺", "Flower"),
                StickerItem("f5", "🪅", "Celebration Piñata"),
                StickerItem("f6", "🎉", "Popper"),
                StickerItem("f7", "🎊", "Confetti"),
                StickerItem("f8", "✨ शुभ लाभ ✨", "Shubh Laabh", 0xFFFEF08A),
                StickerItem("f9", "🌟 जय श्री राम", "Jai Shri Ram", 0xFFFDBA74),
                StickerItem("f10", "🕉️", "Om"),
                StickerItem("f11", "🎁", "Gift Box"),
                StickerItem("f12", "🥳", "Party Face")
            )
        ),
        StickerCategory(
            id = "sports",
            name = "Sports",
            icon = "🏆",
            stickers = listOf(
                StickerItem("sp1", "🏆", "Trophy"),
                StickerItem("sp2", "🥇", "Gold Medal"),
                StickerItem("sp3", "🥈", "Silver Medal"),
                StickerItem("sp4", "🏏", "Cricket"),
                StickerItem("sp5", "⚽", "Football"),
                StickerItem("sp6", "🏀", "Basketball"),
                StickerItem("sp7", "🥊", "Boxing"),
                StickerItem("sp8", "🎯", "Bullseye"),
                StickerItem("sp9", "CHAMPION 🏆", "Champion Badge", 0xFFF59E0B)
            )
        ),
        StickerCategory(
            id = "decorative",
            name = "Decorative",
            icon = "✨",
            stickers = listOf(
                StickerItem("d1", "✦ ✦ ✦", "Star Row", 0xFFFCD34D),
                StickerItem("d2", "══════", "Divider Line", 0xFFE2E8F0),
                StickerItem("d3", "◄◄ PREV", "Prev Button", 0xFF6366F1),
                StickerItem("d4", "NEXT ►►", "Next Button", 0xFF6366F1),
                StickerItem("d5", "SWIPE UP ⬆️", "Swipe Up", 0xFFEC4899),
                StickerItem("d6", "SWIPE LEFT ⬅️", "Swipe Left", 0xFF8B5CF6),
                StickerItem("d7", "⭐ ⭐ ⭐ ⭐ ⭐", "5 Stars Rating", 0xFFFBBF24),
                StickerItem("d8", "• MUST TRY •", "Must Try", 0xFF14B8A6)
            )
        )
    )
}
