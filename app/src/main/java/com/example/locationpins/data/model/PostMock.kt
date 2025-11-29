package com.example.locationpins.data.model

object PostMock {
    val samplePosts = listOf(

        Post(
            postId = "1",
            pinId = "pin_01",
            title = "Cầu Vàng Đà Nẵng lúc hoàng hôn",
            body = "Đứng trên Cầu Vàng mà cảm giác như lạc vào thế giới thần tiên. Bàn tay khổng lồ nâng cầu giữa mây núi Bà Nà, view cực đỉnh!",
            imageUrl = "https://images.unsplash.com/photo-1738627760098-51b0df6f2ac9?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Thay mới: Cầu Vàng Đà Nẵng thực tế
            reactCount = 12485,
            commentCount = 892,
            tags = listOf("Đà Nẵng", "Cầu Vàng", "Travel", "Sunset")
        ),
        Post(
            postId = "2",
            pinId = "pin_02",
            title = "Phở bò Hà Nội sáng sớm",
            body = "Không gì đánh bại được tô phở bò nóng hổi ở phố cổ. Nước dùng ngọt thanh, bánh đa dai dai, hành lá thơm lừng.",
            imageUrl = "https://images.unsplash.com/photo-1631709497146-a239ef373cf1?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Giữ: Núi tuyết (gợi Hà Nội se lạnh)
            reactCount = 8932,
            commentCount = 567,
            tags = listOf("Ẩm thực", "Hà Nội", "Phở", "Food")
        ),
        Post(
            postId = "3",
            pinId = "pin_03",
            title = "Vịnh Hạ Long từ flycam",
            body = "Hơn 1.900 hòn đảo nổi lên giữa làn nước xanh ngọc. Di sản thế giới không phụ lòng người đến thăm.",
            imageUrl = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=500&ixlib=rb-4.0.3",  // Thay mới: Vịnh Hạ Long drone view
            reactCount = 25671,
            commentCount = 1340,
            tags = listOf("Hạ Long", "Quảng Ninh", "Drone", "UNESCO")
        ),
        Post(
            postId = "4",
            pinId = "pin_04",
            title = "Hội An về đêm lung linh",
            body = "Thả đèn hoa đăng trên sông Hoài, đèn lồng rực rỡ khắp phố cổ. Cảm giác như bước vào phim cổ trang vậy.",
            imageUrl = "https://images.unsplash.com/photo-1539898562396-8f2e4c4e6b2c",  // Giữ: Núi xanh (gợi cảnh Hội An)
            reactCount = 18753,
            commentCount = 1021,
            tags = listOf("Hội An", "Đèn lồng", "Night", "Heritage")
        ),
        Post(
            postId = "5",
            pinId = "pin_05",
            title = "Cà phê trứng giữa lòng Hà Nội",
            body = "Trời se lạnh mà ngồi nhâm nhi ly cà phê trứng béo ngậy ở quán Giảng thì hết sảy!",
            imageUrl = "https://images.unsplash.com/photo-1517248135467-10512e4a9f73",  // Giữ: Người ngắm sương mù (fit Hà Nội)
            reactCount = 6721,
            commentCount = 489,
            tags = listOf("Cà phê", "Hà Nội", "Egg Coffee", "Drink")
        ),
        Post(
            postId = "6",
            pinId = "pin_06",
            title = "Ruộng bậc thang Mù Cang Chải mùa nước đổ",
            body = "Mùa nước đổ tháng 5-6, cả thung lũng ánh lên như tấm gương khổng lồ phản chiếu trời xanh.",
            imageUrl = "https://images.unsplash.com/photo-1506894321111-1c7e9d2c5bc9",  // Giữ: Người tuyết (gợi núi cao)
            reactCount = 29841,
            commentCount = 1876,
            tags = listOf("Mù Cang Chải", "Yên Bái", "Terraced Field", "Nature")
        ),
        Post(
            postId = "7",
            pinId = "pin_07",
            title = "Bánh mì Sài Gòn - đỉnh cao ẩm thực đường phố",
            body = "Ổ bánh mì pate + thịt nướng + dưa chua + rau thơm = thiên đường chỉ 25k!",
            imageUrl = "https://images.unsplash.com/photo-1567389782167-c8af6af72c81",  // Lỗi -> Thay mới
            reactCount = 15682,
            commentCount = 923,
            tags = listOf("Bánh mì", "Sài Gòn", "Street Food", "Vietnam")
        ),
        Post(
            postId = "8",
            pinId = "pin_08",
            title = "Đèo Ô Quy Hồ - cung đường đẹp nhất Tây Bắc",
            body = "Đứng trên đỉnh đèo nhìn xuống thung lũng mây trắng bồng bềnh, gió lạnh buốt nhưng đáng giá từng giây.",
            imageUrl = "https://images.unsplash.com/photo-1519904981064-b0cfaffc988c",  // Giữ: Hoa poppy đỏ (gợi hoa dại núi)
            reactCount = 13456,
            commentCount = 782,
            tags = listOf("Ô Quy Hồ", "Lào Cai", "Mountain Pass", "Motorcycle")
        ),
        Post(
            postId = "9",
            pinId = "pin_09",
            title = "Santorini của Việt Nam - Lý Sơn",
            body = "Nhà thờ vòng cung trắng xanh ở đảo Bé, nước trong veo nhìn thấy san hô luôn!",
            imageUrl = "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=500&ixlib=rb-4.0.3",  // Thay mới: Đảo Lý Sơn xanh ngọc
            reactCount = 18923,
            commentCount = 1102,
            tags = listOf("Lý Sơn", "Quảng Ngãi", "Island", "Sea")
        ),
        Post(
            postId = "10",
            pinId = "pin_10",
            title = "Sapa những ngày sương mù",
            body = "Cả thị trấn chìm trong sương, chỉ còn lại mái ngói âm dương và khói bếp Hoàng Liên Sơn.",
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e",  // Giữ: Biển (nhưng fit sương mù? Giữ tạm)
            reactCount = 21344,
            commentCount = 987,
            tags = listOf("Sapa", "Fog", "Morning", "Mountain")
        ),
        Post(
            postId = "11",
            pinId = "pin_11",
            title = "Chợ nổi Cái Răng - văn hóa miền Tây sông nước",
            body = "5h sáng đã nghe tiếng chào mời rộn ràng, xuồng ghe chở đầy trái cây tươi rói.",
            imageUrl = "https://images.unsplash.com/photo-1738627760098-51b0df6f2ac9?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            reactCount = 9876,
            commentCount = 543,
            tags = listOf("Cần Thơ", "Chợ nổi", "Mekong Delta", "Culture")
        ),
        Post(
            postId = "12",
            pinId = "pin_12",
            title = "Phú Quốc - hoàng hôn ở Dinh Cậu",
            body = "Hoàng hôn đỏ rực nhuộm cả bầu trời và mặt biển, ngồi đây nhậu mực nướng là hết bài.",
            imageUrl = "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=500&ixlib=rb-4.0.3",
            reactCount = 27891,
            commentCount = 1567,
            tags = listOf("Phú Quốc", "Sunset", "Seafood", "Island")
        ),

        Post(
            postId = "13",
            pinId = "pin_13",
            title = "Cốm làng Vòng mùa thu Hà Nội",
            body = "Mùi cốm mới thoảng trong ngõ nhỏ, gói trong lá sen ăn kèm chuối tiêu là nhớ cả tuổi thơ.",
            imageUrl = "https://images.unsplash.com/photo-1606890653315-9478e9f42367",  // Giữ: Hoa poppy (gợi mùa thu)
            reactCount = 11234,
            commentCount = 678,
            tags = listOf("Hà Nội", "Cốm", "Ẩm thực", "Mùa thu")
        ),
        Post(
            postId = "14",
            pinId = "pin_14",
            title = "Cốc cà phê vỉa hè Sài Gòn mưa lất phất",
            body = "Ngồi ghế nhựa, nghe nhạc Trịnh, nhìn xe cộ chạy qua dưới mưa phùn… Sài Gòn là đây chứ đâu.",
            imageUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5956?w=500&ixlib=rb-4.0.3",  // Thay mới: Cà phê mưa Sài Gòn
            reactCount = 19876,
            commentCount = 1345,
            tags = listOf("Sài Gòn", "Cà phê vỉa hè", "Rainy", "Mood")
        ),

        Post(
            postId = "15",
            pinId = "pin_15",
            title = "Đồi chè trái tim Mộc Châu",
            body = "Những trái tim xanh mướt giữa cao nguyên, chụp ảnh cưới hay sống ảo đều auto đẹp.",
            imageUrl = "https://images.unsplash.com/photo-1572203944246-247a3e5da7f8?w=500&ixlib=rb-4.0.3",  // Thay mới: Đồi chè Mộc Châu
            reactCount = 34219,
            commentCount = 2103,
            tags = listOf("Mộc Châu", "Đồi chè", "Check-in", "Love")
        ),
        Post(
            postId = "1",
            pinId = "pin_01",
            title = "Cầu Vàng Đà Nẵng lúc hoàng hôn",
            body = "Đứng trên Cầu Vàng mà cảm giác như lạc vào thế giới thần tiên. Bàn tay khổng lồ nâng cầu giữa mây núi Bà Nà, view cực đỉnh!",
            imageUrl = "https://images.unsplash.com/photo-1738627760098-51b0df6f2ac9?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Thay mới: Cầu Vàng Đà Nẵng thực tế
            reactCount = 12485,
            commentCount = 892,
            tags = listOf("Đà Nẵng", "Cầu Vàng", "Travel", "Sunset")
        ),
        Post(
            postId = "2",
            pinId = "pin_02",
            title = "Phở bò Hà Nội sáng sớm",
            body = "Không gì đánh bại được tô phở bò nóng hổi ở phố cổ. Nước dùng ngọt thanh, bánh đa dai dai, hành lá thơm lừng.",
            imageUrl = "https://images.unsplash.com/photo-1631709497146-a239ef373cf1?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Giữ: Núi tuyết (gợi Hà Nội se lạnh)
            reactCount = 8932,
            commentCount = 567,
            tags = listOf("Ẩm thực", "Hà Nội", "Phở", "Food")
        ),
        Post(
            postId = "3",
            pinId = "pin_03",
            title = "Vịnh Hạ Long từ flycam",
            body = "Hơn 1.900 hòn đảo nổi lên giữa làn nước xanh ngọc. Di sản thế giới không phụ lòng người đến thăm.",
            imageUrl = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=500&ixlib=rb-4.0.3",  // Thay mới: Vịnh Hạ Long drone view
            reactCount = 25671,
            commentCount = 1340,
            tags = listOf("Hạ Long", "Quảng Ninh", "Drone", "UNESCO")
        ),
        Post(
            postId = "1",
            pinId = "pin_01",
            title = "Cầu Vàng Đà Nẵng lúc hoàng hôn",
            body = "Đứng trên Cầu Vàng mà cảm giác như lạc vào thế giới thần tiên. Bàn tay khổng lồ nâng cầu giữa mây núi Bà Nà, view cực đỉnh!",
            imageUrl = "https://images.unsplash.com/photo-1738627760098-51b0df6f2ac9?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Thay mới: Cầu Vàng Đà Nẵng thực tế
            reactCount = 12485,
            commentCount = 892,
            tags = listOf("Đà Nẵng", "Cầu Vàng", "Travel", "Sunset")
        ),
        Post(
            postId = "2",
            pinId = "pin_02",
            title = "Phở bò Hà Nội sáng sớm",
            body = "Không gì đánh bại được tô phở bò nóng hổi ở phố cổ. Nước dùng ngọt thanh, bánh đa dai dai, hành lá thơm lừng.",
            imageUrl = "https://images.unsplash.com/photo-1631709497146-a239ef373cf1?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Giữ: Núi tuyết (gợi Hà Nội se lạnh)
            reactCount = 8932,
            commentCount = 567,
            tags = listOf("Ẩm thực", "Hà Nội", "Phở", "Food")
        ),
        Post(
            postId = "3",
            pinId = "pin_03",
            title = "Vịnh Hạ Long từ flycam",
            body = "Hơn 1.900 hòn đảo nổi lên giữa làn nước xanh ngọc. Di sản thế giới không phụ lòng người đến thăm.",
            imageUrl = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=500&ixlib=rb-4.0.3",  // Thay mới: Vịnh Hạ Long drone view
            reactCount = 25671,
            commentCount = 1340,
            tags = listOf("Hạ Long", "Quảng Ninh", "Drone", "UNESCO")
        ),
        Post(
            postId = "1",
            pinId = "pin_01",
            title = "Cầu Vàng Đà Nẵng lúc hoàng hôn",
            body = "Đứng trên Cầu Vàng mà cảm giác như lạc vào thế giới thần tiên. Bàn tay khổng lồ nâng cầu giữa mây núi Bà Nà, view cực đỉnh!",
            imageUrl = "https://images.unsplash.com/photo-1738627760098-51b0df6f2ac9?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Thay mới: Cầu Vàng Đà Nẵng thực tế
            reactCount = 12485,
            commentCount = 892,
            tags = listOf("Đà Nẵng", "Cầu Vàng", "Travel", "Sunset")
        ),
        Post(
            postId = "2",
            pinId = "pin_02",
            title = "Phở bò Hà Nội sáng sớm",
            body = "Không gì đánh bại được tô phở bò nóng hổi ở phố cổ. Nước dùng ngọt thanh, bánh đa dai dai, hành lá thơm lừng.",
            imageUrl = "https://images.unsplash.com/photo-1631709497146-a239ef373cf1?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Giữ: Núi tuyết (gợi Hà Nội se lạnh)
            reactCount = 8932,
            commentCount = 567,
            tags = listOf("Ẩm thực", "Hà Nội", "Phở", "Food")
        ),
        Post(
            postId = "3",
            pinId = "pin_03",
            title = "Vịnh Hạ Long từ flycam",
            body = "Hơn 1.900 hòn đảo nổi lên giữa làn nước xanh ngọc. Di sản thế giới không phụ lòng người đến thăm.",
            imageUrl = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=500&ixlib=rb-4.0.3",  // Thay mới: Vịnh Hạ Long drone view
            reactCount = 25671,
            commentCount = 1340,
            tags = listOf("Hạ Long", "Quảng Ninh", "Drone", "UNESCO")
        ),
        Post(
            postId = "1",
            pinId = "pin_01",
            title = "Cầu Vàng Đà Nẵng lúc hoàng hôn",
            body = "Đứng trên Cầu Vàng mà cảm giác như lạc vào thế giới thần tiên. Bàn tay khổng lồ nâng cầu giữa mây núi Bà Nà, view cực đỉnh!",
            imageUrl = "https://images.unsplash.com/photo-1738627760098-51b0df6f2ac9?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Thay mới: Cầu Vàng Đà Nẵng thực tế
            reactCount = 12485,
            commentCount = 892,
            tags = listOf("Đà Nẵng", "Cầu Vàng", "Travel", "Sunset")
        ),
        Post(
            postId = "2",
            pinId = "pin_02",
            title = "Phở bò Hà Nội sáng sớm",
            body = "Không gì đánh bại được tô phở bò nóng hổi ở phố cổ. Nước dùng ngọt thanh, bánh đa dai dai, hành lá thơm lừng.",
            imageUrl = "https://images.unsplash.com/photo-1631709497146-a239ef373cf1?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",  // Giữ: Núi tuyết (gợi Hà Nội se lạnh)
            reactCount = 8932,
            commentCount = 567,
            tags = listOf("Ẩm thực", "Hà Nội", "Phở", "Food")
        ),
        Post(
            postId = "3",
            pinId = "pin_03",
            title = "Vịnh Hạ Long từ flycam",
            body = "Hơn 1.900 hòn đảo nổi lên giữa làn nước xanh ngọc. Di sản thế giới không phụ lòng người đến thăm.",
            imageUrl = "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=500&ixlib=rb-4.0.3",  // Thay mới: Vịnh Hạ Long drone view
            reactCount = 25671,
            commentCount = 1340,
            tags = listOf("Hạ Long", "Quảng Ninh", "Drone", "UNESCO")
        ),

    )
}